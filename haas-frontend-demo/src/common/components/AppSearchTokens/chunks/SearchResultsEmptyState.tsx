import { defaultNS } from "@app/common/locales/locales";
import { useTranslation } from "react-i18next";

type SearchResultsEmptyStateProps = {
  searchKeyword: string;
};

function SearchResultsEmptyState({ searchKeyword }: SearchResultsEmptyStateProps) {
  const { t } = useTranslation(defaultNS, { keyPrefix: "general.appSearchTokens" });

  return (
    <div className='searchEmptyState'>
      <div className='icon'>
        <i className='pi pi-search'></i>
      </div>
      <div className='caption'>
        <p className='text-16'>
          {t("notFoundTitle")}
          {searchKeyword.length > 0 && <strong className='color--primary-600 mx-8'>"{searchKeyword}"</strong>}
        </p>
        <p className='text-14 text-w400 color--gray-700'>{t("notFoundCaption")}</p>
      </div>
    </div>
  );
}

export default SearchResultsEmptyState;
