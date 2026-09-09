import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import test from "node:test";
import { checkWorkspace } from "./check-boundaries.mjs";

function syntheticWorkspace() {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), "penatika-boundaries-"));

  for (const appName of ["teacher", "display"]) {
    fs.mkdirSync(path.join(root, "apps", appName, "src"), { recursive: true });
    fs.writeFileSync(
      path.join(root, "apps", appName, "package.json"),
      JSON.stringify({ name: `@penatika/${appName}`, private: true, dependencies: {} }),
    );
  }

  return root;
}

function withSyntheticWorkspace(run) {
  const root = syntheticWorkspace();
  try {
    run(root);
  } finally {
    fs.rmSync(root, { recursive: true, force: true });
  }
}

test("rejects Display importing Teacher by relative path", () => {
  withSyntheticWorkspace((root) => {
    fs.writeFileSync(
      path.join(root, "apps", "display", "src", "invalid.ts"),
      'import "../../teacher/src/private";\n',
    );

    assert.ok(checkWorkspace(root).some((violation) => violation.includes("teacher application by relative path")));
  });
});

test("rejects Teacher importing the Display package", () => {
  withSyntheticWorkspace((root) => {
    fs.writeFileSync(
      path.join(root, "apps", "teacher", "src", "invalid.ts"),
      'export { privateDisplayState } from "@penatika/display/private";\n',
    );

    assert.ok(checkWorkspace(root).some((violation) => violation.includes("teacher source imports the display")));
  });
});

test("rejects an unapproved private workspace dependency", () => {
  withSyntheticWorkspace((root) => {
    const manifestPath = path.join(root, "apps", "display", "package.json");
    fs.writeFileSync(
      manifestPath,
      JSON.stringify({
        name: "@penatika/display",
        private: true,
        dependencies: { "@penatika/transport-teacher": "workspace:*" },
      }),
    );

    assert.ok(checkWorkspace(root).some((violation) => violation.includes("unapproved private package")));
  });
});

test("rejects a string-literal dynamic import across applications", () => {
  withSyntheticWorkspace((root) => {
    fs.writeFileSync(
      path.join(root, "apps", "display", "src", "invalid.ts"),
      'void import("@penatika/teacher/private");\n',
    );

    assert.ok(checkWorkspace(root).some((violation) => violation.includes("display source imports the teacher")));
  });
});

test("rejects an arbitrary shared workspace path", () => {
  withSyntheticWorkspace((root) => {
    fs.writeFileSync(
      path.join(root, "apps", "teacher", "src", "invalid.ts"),
      'import { sharedState } from "web/shared/state";\n',
    );

    assert.ok(checkWorkspace(root).some((violation) => violation.includes("unreviewed shared path")));
  });
});
