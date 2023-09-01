import { useLocation, useSearchParams } from "react-router-dom";

export default function Callback() {
  const [searchParams] = useSearchParams();
  const twitterCode = searchParams.get("code") ? searchParams.get("code") : "";

  if (twitterCode) {
    localStorage.setItem("twitterCode", twitterCode);
    window.close();
  }

  return <></>;
}
