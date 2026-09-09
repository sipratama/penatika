import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { fileURLToPath, pathToFileURL } from "node:url";
import { API } from "typescript/unstable/sync";
import * as ts from "typescript/unstable/ast";

const APP_NAMES = ["teacher", "display"];
const PRIVATE_PACKAGE_ALLOWLIST = {
  teacher: new Set(["@penatika/transport-teacher"]),
  display: new Set(["@penatika/transport-display"]),
};
const SOURCE_EXTENSIONS = new Set([".ts", ".tsx", ".mts", ".cts"]);

function isWithin(candidate, directory) {
  const relative = path.relative(directory, candidate);
  return relative === "" || (!relative.startsWith("..") && !path.isAbsolute(relative));
}

function collectSourceFiles(directory) {
  if (!fs.existsSync(directory)) {
    return [];
  }

  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      return collectSourceFiles(entryPath);
    }
    return SOURCE_EXTENSIONS.has(path.extname(entry.name)) ? [entryPath] : [];
  });
}

function collectModuleSpecifiers(sourceFile) {
  const specifiers = [];

  function visit(node) {
    if ((ts.isImportDeclaration(node) || ts.isExportDeclaration(node))
        && node.moduleSpecifier
        && ts.isStringLiteralLikeNode(node.moduleSpecifier)) {
      specifiers.push(node.moduleSpecifier.text);
    }

    if (ts.isCallExpression(node)
        && node.expression.kind === ts.SyntaxKind.ImportKeyword
        && node.arguments.length === 1
        && ts.isStringLiteralLikeNode(node.arguments[0])) {
      specifiers.push(node.arguments[0].text);
    }

    node.forEachChild(visit);
  }

  visit(sourceFile);
  return specifiers;
}

function inspectSpecifier({ appName, filePath, specifier, workspaceRoot }) {
  const violations = [];
  const siblingName = appName === "teacher" ? "display" : "teacher";
  const siblingPackage = `@penatika/${siblingName}`;
  const forbiddenSharedSpecifiers = ["web/shared", "web/common", "web/src/common"];

  if (specifier === siblingPackage || specifier.startsWith(`${siblingPackage}/`)) {
    violations.push(`${appName} source imports the ${siblingName} application package`);
  }

  if (specifier.startsWith("@penatika/")
      && !PRIVATE_PACKAGE_ALLOWLIST[appName].has(specifier.split("/").slice(0, 2).join("/"))) {
    violations.push(`${appName} source imports unapproved private package ${specifier}`);
  }

  if (forbiddenSharedSpecifiers.some((root) => specifier === root || specifier.startsWith(`${root}/`))) {
    violations.push(`${appName} source imports from an unreviewed shared path`);
  }

  if (specifier.startsWith(".")) {
    const resolved = path.resolve(path.dirname(filePath), specifier);
    const siblingRoot = path.join(workspaceRoot, "apps", siblingName);
    const forbiddenSharedRoots = [
      path.join(workspaceRoot, "shared"),
      path.join(workspaceRoot, "common"),
      path.join(workspaceRoot, "src", "common"),
    ];

    if (isWithin(resolved, siblingRoot)) {
      violations.push(`${appName} source imports from the ${siblingName} application by relative path`);
    }
    if (forbiddenSharedRoots.some((root) => isWithin(resolved, root))) {
      violations.push(`${appName} source imports from an unreviewed shared path`);
    }
  }

  return violations;
}

function inspectManifest(appName, workspaceRoot) {
  const manifestPath = path.join(workspaceRoot, "apps", appName, "package.json");
  const manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));
  const siblingName = appName === "teacher" ? "display" : "teacher";
  const siblingPackage = `@penatika/${siblingName}`;
  const dependencyGroups = ["dependencies", "devDependencies", "peerDependencies", "optionalDependencies"];
  const violations = [];

  for (const group of dependencyGroups) {
    for (const dependency of Object.keys(manifest[group] ?? {})) {
      if (dependency === siblingPackage) {
        violations.push(`${path.relative(workspaceRoot, manifestPath)} declares the sibling ${siblingPackage} in ${group}`);
      } else if (dependency.startsWith("@penatika/")
          && !PRIVATE_PACKAGE_ALLOWLIST[appName].has(dependency)) {
        violations.push(`${path.relative(workspaceRoot, manifestPath)} declares unapproved private package ${dependency} in ${group}`);
      }
    }
  }

  return violations;
}

export function checkWorkspace(workspaceRoot) {
  const normalizedRoot = path.resolve(workspaceRoot);
  const violations = [];
  const sourceFilesByApp = Object.fromEntries(APP_NAMES.map((appName) => [
    appName,
    collectSourceFiles(path.join(normalizedRoot, "apps", appName, "src")),
  ]));
  const api = new API({ cwd: normalizedRoot });
  const snapshot = api.updateSnapshot({ openFiles: Object.values(sourceFilesByApp).flat() });

  try {
    for (const appName of APP_NAMES) {
      violations.push(...inspectManifest(appName, normalizedRoot));

      for (const filePath of sourceFilesByApp[appName]) {
        const project = snapshot.getDefaultProjectForFile(filePath);
        const sourceFile = project?.program.getSourceFile(filePath);
        if (!sourceFile) {
          violations.push(`${path.relative(normalizedRoot, filePath)}: TypeScript could not parse source file`);
          continue;
        }

        for (const specifier of collectModuleSpecifiers(sourceFile)) {
          for (const message of inspectSpecifier({ appName, filePath, specifier, workspaceRoot: normalizedRoot })) {
            violations.push(`${path.relative(normalizedRoot, filePath)}: ${message}`);
          }
        }
      }
    }
  } finally {
    snapshot.dispose();
    api.close();
  }

  return violations;
}

function runCli() {
  const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
  const workspaceRoot = path.resolve(scriptDirectory, "..");
  const violations = checkWorkspace(workspaceRoot);

  if (violations.length > 0) {
    console.error("Frontend boundary violations:\n" + violations.map((violation) => `- ${violation}`).join("\n"));
    process.exitCode = 1;
    return;
  }

  console.log("Frontend boundaries valid: Teacher and Display remain private siblings.");
}

if (import.meta.url === pathToFileURL(process.argv[1]).href) {
  runCli();
}
