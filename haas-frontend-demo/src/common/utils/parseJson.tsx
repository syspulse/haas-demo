export default function parseJson(str: string, fix?: boolean) {
  let result = {
    isObject: false,
    output: str,
  };

  try {
    result = {
      isObject: true,
      output: JSON.parse(str),
    };
  } catch (err) {
    str[0] === "{" && str[str.length - 1] === "}" && (result = parseJson(str.replace(/\'/g, '"'), true));
  }

  return result;
}

export function objectToTree(input: any, collapsed?: boolean, cb?: any, id?: any): any {
  input = typeof input === "string" ? input : JSON.stringify(input);
  const parsed = parseJson(input);

  return (
    <>
      {cb && parsed.isObject && (
        <i
          className={`expand pi pi-${!collapsed ? "minus" : "plus"}`}
          onClick={() => {
            cb((prev: any) => {
              return !collapsed ? [...prev].filter((item: any) => item !== id) : [...prev, id];
            });
          }}
        ></i>
      )}
      {parsed.isObject ? (
        collapsed ? (
          <>&#123; {Object.entries(parsed.output)[0]} ...&#125;</>
        ) : (
          <>
            &#123;
            <ul>
              {Object.entries(parsed.output).map((item: any) => (
                <li key={item[0]}>
                  <strong>{item[0]}</strong>: {objectToTree(item[1])}
                </li>
              ))}
            </ul>
            &#125;
          </>
        )
      ) : (
        parsed.output
      )}
    </>
  );
}
