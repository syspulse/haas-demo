const fs = require("fs/promises");

async function deployVersion() {
  try {
    const data = await fs.readFile("./src/common/config/deploys.json", { encoding: "utf8" });
    const content = data ? JSON.parse(data) : [];
    revision = require("child_process").execSync("git rev-parse HEAD").toString().trim();
    content.push(revision);

    await fs.writeFile("./src/common/config/deploys.json", JSON.stringify(content));
  } catch (err) {
    await fs.writeFile("./src/common/config/deploys.json", []);
    console.log('No version file restart...');
    deployVersion();
  }
}

deployVersion();
