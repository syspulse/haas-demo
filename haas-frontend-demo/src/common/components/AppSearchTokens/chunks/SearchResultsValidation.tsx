import { defaultNS } from "@app/common/locales/locales";
import { useTranslation } from "react-i18next";

function SearchResultsValidation({ isError }: { isError: boolean }) {
  const { t } = useTranslation(defaultNS, { keyPrefix: "general.appSearchTokens" });

  return <p className='searchValidation'>{isError ? t("somethingWentWrong") : t("validation")}</p>;
}

export default SearchResultsValidation;
