import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";
import openapiTS, { astToString } from "openapi-typescript";

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const repositoryRoot = path.resolve(packageRoot, "../..");
const bundledContractPath = path.join(packageRoot, ".tmp", "openapi.dereferenced.json");
const HTTP_METHODS = new Set(["get", "put", "post", "delete", "options", "head", "patch", "trace"]);

const roles = {
  teacher: {
    operations: new Set([
      "getAuthenticatedTeacherSession",
      "startClassroomSession",
      "createPairingGrant",
      "revokePairingGrant",
      "establishTeacherControllerParticipant",
      "getClassroomControllerState",
      "submitClassroomSessionCommand",
    ]),
    securitySchemes: new Set(["TeacherBrowserSession", "ParticipantBrowserSession"]),
    output: path.join(repositoryRoot, "web", "packages", "transport-teacher", "src", "index.d.ts"),
  },
  display: {
    operations: new Set([
      "establishClassroomDisplayParticipant",
      "getClassroomDisplaySnapshot",
      "streamClassroomDisplayEvents",
      "acknowledgeClassroomDisplaySynchronization",
    ]),
    securitySchemes: new Set(["ParticipantBrowserSession"]),
    output: path.join(repositoryRoot, "web", "packages", "transport-display", "src", "index.d.ts"),
  },
};

function deriveRoleContract(canonicalContract, role) {
  const roleContract = structuredClone(canonicalContract);
  roleContract.paths = {};
  const foundOperations = new Set();

  for (const [pathName, pathItem] of Object.entries(canonicalContract.paths ?? {})) {
    const retainedPathItem = {};
    for (const [key, value] of Object.entries(pathItem)) {
      if (!HTTP_METHODS.has(key)) {
        retainedPathItem[key] = value;
      } else if (role.operations.has(value.operationId)) {
        retainedPathItem[key] = value;
        foundOperations.add(value.operationId);
      }
    }

    if (Object.keys(retainedPathItem).some((key) => HTTP_METHODS.has(key))) {
      roleContract.paths[pathName] = retainedPathItem;
    }
  }

  assert.deepEqual(foundOperations, role.operations, "canonical contract operation allowlist drifted");
  const canonicalSecuritySchemes = canonicalContract.components?.securitySchemes ?? {};
  roleContract.components = {
    securitySchemes: Object.fromEntries([...role.securitySchemes].map((name) => {
      assert.ok(canonicalSecuritySchemes[name], `canonical security scheme ${name} is missing`);
      return [name, canonicalSecuritySchemes[name]];
    })),
  };
  delete roleContract.webhooks;
  return roleContract;
}

function operationIds(contract) {
  return Object.values(contract.paths).flatMap((pathItem) => Object.entries(pathItem)
    .filter(([key]) => HTTP_METHODS.has(key))
    .map(([, operation]) => operation.operationId));
}

function securitySchemeNames(contract) {
  return new Set(Object.values(contract.paths).flatMap((pathItem) => Object.entries(pathItem)
    .filter(([key]) => HTTP_METHODS.has(key))
    .flatMap(([, operation]) => (operation.security ?? []).flatMap(Object.keys))));
}

async function generate(roleName, canonicalContract) {
  const role = roles[roleName];
  const subset = deriveRoleContract(canonicalContract, role);
  const subsetPath = path.join(packageRoot, ".tmp", `${roleName}.openapi.json`);
  fs.writeFileSync(subsetPath, JSON.stringify(subset, null, 2) + "\n");

  assert.deepEqual(new Set(operationIds(subset)), role.operations);
  assert.deepEqual(new Set(Object.keys(subset.components.securitySchemes)), role.securitySchemes);
  assert.deepEqual(securitySchemeNames(subset), role.securitySchemes);
  const first = astToString(await openapiTS(subset));
  const second = astToString(await openapiTS(structuredClone(subset)));
  assert.equal(first, second, `${roleName} generation must be byte-for-byte deterministic`);

  for (const operation of role.operations) {
    assert.match(first, new RegExp(`\\b${operation}\\b`));
  }
  const oppositeRole = roleName === "teacher" ? roles.display : roles.teacher;
  for (const operation of oppositeRole.operations) {
    assert.doesNotMatch(first, new RegExp(`\\b${operation}\\b`));
  }
  if (roleName === "display") {
    for (const projectionMember of ["schemaVersion", "classroomSessionId", "revision", "scene", "PLAIN_TEXT"]) {
      assert.match(first, new RegExp(`\\b${projectionMember}\\b`));
    }
  }

  return first;
}

if (!fs.existsSync(bundledContractPath)) {
  throw new Error("Run npm run bundle:openapi before generating transport declarations.");
}

const canonicalContract = JSON.parse(fs.readFileSync(bundledContractPath, "utf8"));
const generated = Object.fromEntries(await Promise.all(
  Object.keys(roles).map(async (roleName) => [roleName, await generate(roleName, canonicalContract)]),
));

if (process.argv.includes("--write")) {
  for (const [roleName, content] of Object.entries(generated)) {
    fs.mkdirSync(path.dirname(roles[roleName].output), { recursive: true });
    fs.writeFileSync(roles[roleName].output, content);
  }
  console.log("Generated deterministic Teacher and Display transport declarations.");
} else if (process.argv.includes("--check")) {
  for (const [roleName, content] of Object.entries(generated)) {
    assert.equal(
      fs.readFileSync(roles[roleName].output, "utf8"),
      content,
      `${roleName} generated transport declarations are stale`,
    );
  }
  console.log("Generated transport declarations are deterministic, role-isolated, and current.");
} else {
  throw new Error("Expected --write or --check.");
}
