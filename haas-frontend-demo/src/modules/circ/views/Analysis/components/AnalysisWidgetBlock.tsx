import { ReactElement } from "react";

const AnalysisWidgetBlock = ({
  children,
  orientation,
  maxWidth,
}: {
  children: ReactElement[] | ReactElement;
  orientation?: "vertical" | "horizontal";
  maxWidth?: string;
}) => {
  if (orientation === "vertical") {
    return (
      <div className="gridBlockVertical" style={{ maxWidth }}>
        {children}
      </div>
    );
  }
  return (
    <>
      {children}
      <div className="flex-basis--100"></div>
    </>
  );
};

export default AnalysisWidgetBlock;
