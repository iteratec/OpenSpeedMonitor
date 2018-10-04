import {parseDate} from "./date.util";

describe("parseDate", () => {
  it("creates a new date instance from an existing date", () => {
    const date = new Date("2018-09-31T06:30:56.876Z");
    const result = parseDate(date);
    expect(result).not.toBe(date);
    expect(result.getTime()).toEqual(date.getTime());
  });

  it("creates a new date instance from an ISO time string", () => {
    const isoString = "2018-09-31T06:30:56.876Z";
    expect(parseDate(isoString).getTime()).toEqual(new Date(isoString).getTime());
  });

  it("creates a new date instance from an ISO date string at UTC", () => {
    const expected = new Date("2018-09-31T00:00:00.0Z");
    expect(parseDate("2018-09-31").getTime()).toEqual(new Date(expected).getTime());
  });
});
