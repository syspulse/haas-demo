export default function parseStream(latestAlarm: any) {
  const parse = latestAlarm.split("\n");

  return parse
    .reduce((acc: any[], item: string) => {
      if (item.length > 2) {
        const tx: any = JSON.parse(item);
        acc.push(tx);
      }
      return acc;
    }, []);
}
