import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";
import Ajv2020 from "ajv/dist/2020.js";

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const repositoryRoot = path.resolve(packageRoot, "../..");
const schemasRoot = path.join(repositoryRoot, "contracts", "schemas");
const fixturesRoot = path.join(packageRoot, "fixtures");

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

const primitives = readJson(path.join(schemasRoot, "wire-primitives.schema.json"));
const projection = readJson(path.join(schemasRoot, "classroom-display-projection.schema.json"));
const ajv = new Ajv2020({ allErrors: true, strict: true, $data: false });
ajv.addSchema(primitives, primitives.$id);

const validateProjection = ajv.compile(projection);
const validateSessionId = ajv.getSchema("wire-primitives.schema.json#/$defs/ClassroomSessionId");
const validateRevision = ajv.getSchema("wire-primitives.schema.json#/$defs/Revision");

assert.ok(validateSessionId, "canonical ClassroomSessionId schema must resolve by $id");
assert.ok(validateRevision, "canonical Revision schema must resolve by $id");

test("compiles the canonical projection with its canonical primitive $id", () => {
  assert.equal(typeof validateProjection, "function");
});

test("accepts a complete valid Display projection", () => {
  assert.equal(validateProjection(readJson(path.join(fixturesRoot, "valid-projection.json"))), true);
});

test("accepts a valid ClassroomSessionId", () => {
  assert.equal(validateSessionId(readJson(path.join(fixturesRoot, "valid-classroom-session-id.json"))), true);
});

test("accepts a nonnegative Revision", () => {
  assert.equal(validateRevision(readJson(path.join(fixturesRoot, "valid-revision.json"))), true);
});

test("rejects an unknown projection root property", () => {
  assert.equal(validateProjection(readJson(path.join(fixturesRoot, "invalid-unknown-root-property.json"))), false);
});

test("rejects an unsupported block type", () => {
  assert.equal(validateProjection(readJson(path.join(fixturesRoot, "invalid-block-type.json"))), false);
});

test("rejects a ClassroomSessionId containing whitespace", () => {
  assert.equal(validateSessionId(readJson(path.join(fixturesRoot, "invalid-whitespace-session-id.json"))), false);
});

test("rejects a negative Revision", () => {
  assert.equal(validateRevision(readJson(path.join(fixturesRoot, "invalid-negative-revision.json"))), false);
});

test("rejects a Revision above the JavaScript-safe maximum", () => {
  assert.equal(validateRevision(readJson(path.join(fixturesRoot, "invalid-unsafe-revision.json"))), false);
});
