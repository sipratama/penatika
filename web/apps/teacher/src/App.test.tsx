import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import App from "./App";

test("renders Teacher Web app shell identity", () => {
  render(<App />);
  expect(screen.getByText("Penatika Teacher")).toBeInTheDocument();
  expect(screen.getByText("Source scaffolding shell")).toBeInTheDocument();
});
