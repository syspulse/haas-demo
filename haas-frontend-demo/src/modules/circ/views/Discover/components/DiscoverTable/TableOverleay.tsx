import { OverlayPanel } from "primereact/overlaypanel";
import { useEffect, useRef } from "react";

export default function TableOverlay({ action }: any) {
  const overlayPanelRef = useRef<any>();
  useEffect(() => {
    if (action.type && action.type === "listOverlay") {
      overlayPanelRef.current.toggle(action.event);
    }
  }, [action]);

  return (
    <>
      <OverlayPanel ref={overlayPanelRef}>
        <ul className='tableOverlayChainsList'>
          {action?.data &&
            action.data.map((item: any) => (
              <li key={item.name}>
                <img src={item.icon} />
                {item.name}
              </li>
            ))}
        </ul>
      </OverlayPanel>
    </>
  );
}
