import uri from '../util/uri.js';

const parseQueryString = uri.parseQueryString;

describe('tests URI parsing', function () {
  it('parses one parameter', function () {
    // Given:
    const url = "http://localhost/resource?debug=1&sample=2";

    // When:
    const queryParam = parseQueryString(url);

    // Then:
    expect(queryParam["debug"]).toBe("1");
    expect(queryParam["sample"]).toBe("2");
  });
});
