import { TabPanel, TabView } from "primereact/tabview";

import AbiTable from "./components/AbiTable";
import "./styles.scss";

export default function Abi() {
  const TOP = 50;

  return (
    <div className='abiRoot'>
      <TabView>
        <TabPanel header='Contract'>
          <AbiTable entity='contract' top={TOP} />
        </TabPanel>
        <TabPanel header='Function'>
          <AbiTable entity='function' top={TOP} />
        </TabPanel>
        <TabPanel header='Event'>
          <AbiTable entity='event' top={TOP} />
        </TabPanel>
      </TabView>
    </div>
  );
}
