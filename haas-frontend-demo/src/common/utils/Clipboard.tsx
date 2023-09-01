/**
 Clipboard Util
 
 Use to copy and trim long text.

 props:
    text > required > input text
    maxLength > optional > if set will trim text to provided length
*/
import { defaultNS } from "@app/common/locales/locales";
import { classNames } from "primereact/utils";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

type ClipboardPropsType = {
  text: string;
  maxLength?: number;
  hideIcon?: boolean;
};

const Clipboard = ({ text, maxLength, hideIcon }: ClipboardPropsType) => {
  !maxLength && (maxLength = text.length);
  text = text.toString();

  const { t } = useTranslation(defaultNS, { keyPrefix: "general.utils" });
  const isClipBoard = Boolean(navigator.clipboard);
  const isTransform = text.length > maxLength;
  const textToArray = text.split("");

  isTransform && textToArray.splice(maxLength / 2, text.length - maxLength, "...");

  const transformedText = textToArray.join("");
  const [output, setOutput] = useState("");

  useEffect(() => {
    setOutput(transformedText);
  }, [text]);

  const execCopy = (event: React.MouseEvent<HTMLSpanElement, MouseEvent>) => {
    event.stopPropagation();
    if (isClipBoard) {
      setOutput(t("copied") as string);
      navigator.clipboard.writeText(text);

      setTimeout(() => {
        setOutput(transformedText);
      }, 1000);
    }
  };

  return (
    <>
      <span
        data-pr-tooltip={`${isClipBoard ? t("copy") : ""}: "${text}"`}
        data-pr-position='top'
        onClick={(event) => execCopy(event)}
        className={classNames({ clickable: isClipBoard }, "tooltip")}
      >
        {output}
        {isClipBoard && !hideIcon && <i className='pi pi-copy'></i>}
      </span>
    </>
  );
};

export default Clipboard;
