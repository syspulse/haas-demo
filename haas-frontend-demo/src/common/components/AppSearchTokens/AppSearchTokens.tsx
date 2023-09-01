import useDebounce from "@app/common/hooks/useDebounce";
import { defaultNS } from "@app/common/locales/locales";
import { IToken } from "@app/common/models/token";
import { useGetTokenSearchQuery } from "@app/common/services/token.service";
import { createUpdateToken } from "@app/common/store/slice/appControlState";
import { InputText } from "primereact/inputtext";
import { OverlayPanel, OverlayPanelTargetType } from "primereact/overlaypanel";
import { classNames } from "primereact/utils";
import { memo, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import SearchResultsEmptyState from "./chunks/SearchResultsEmptyState";
import SearchResultsListing from "./chunks/SearchResultsListing";
import SearchResultsValidation from "./chunks/SearchResultsValidation";
import "./styles.scss";

function AppSearchTokens() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { t } = useTranslation(defaultNS, { keyPrefix: "general.appSearchTokens" });
  const searchResultsOverlayPanel = useRef<OverlayPanel>(null);
  const searchInputRef = useRef<any>(null);
  const overlayPanelActive = useRef<boolean>(false);
  const searchFormRef = useRef<HTMLFormElement>(null);
  const [searchKeyword, setSearchKeyword] = useState<string>("");
  const [activeItemIndex, setActiveItemIndex] = useState<number>(0);
  const debouncedSearchTerm = useDebounce(searchKeyword, 500);
  const { data, isFetching, isError } = useGetTokenSearchQuery(debouncedSearchTerm, {
    skip: debouncedSearchTerm.length < 3,
  });
  const debouncedIsError = useDebounce(isError, 500);

  const hideSearchResultsOverlayPanel = () => searchResultsOverlayPanel?.current?.hide();
  const showSearchResultsOverlayPanel = () =>
    searchResultsOverlayPanel?.current?.show(null, searchInputRef?.current as OverlayPanelTargetType);

  const navigateToken = (data: IToken[], activeItemIndex: number) => {
    if (data && data.length > 0) {
      const item = data[activeItemIndex] || data[0];
      const path = item.addr ? item.addr : item.id;
      let navigationPath = "circulating/" + path;

      window.location.href.includes("job") && (navigationPath = `job?bid=${item.addr}`);
      window.location.href.includes("holders") && (navigationPath = `holders?bid=${item.addr}`);

      if (window.location.href.includes("token")) {
        dispatch(createUpdateToken({ ...item }));
      } else {
        navigate(navigationPath);
      }

      searchInputRef?.current.blur();
      hideSearchResultsOverlayPanel();
    }
  };

  const submitSearch = (event: Event, data: IToken[] | undefined, activeItemIndex: number) => {
    event.preventDefault();

    if (document.activeElement === searchInputRef.current) {
      navigateToken(data || [], activeItemIndex);
    }
  };

  useEffect(() => {
    window.addEventListener("keydown", (event: KeyboardEvent) => {
      if (event.code === "Slash" && (event.ctrlKey || event.metaKey)) {
        event.stopPropagation();
        event.preventDefault();
        searchInputRef?.current.focus();
      }

      if (event.code === "Enter") {
        event.stopPropagation();
        event.preventDefault();
        searchFormRef?.current?.dispatchEvent(new Event("submit", { cancelable: true, bubbles: true }));
      }
    });
  }, []);

  useEffect(() => {
    if (data) {
      const prevCodes = ["ArrowDown", "ArrowUp"];
      let index = activeItemIndex;
      let prevCode = "";

      window.addEventListener("keyup", (event: KeyboardEvent) => {
        if (overlayPanelActive.current) {
          prevCode = event.code;

          if (prevCodes.includes(prevCode) && (event.code === "ArrowDown" || event.code === "ArrowUp")) {
            event.stopPropagation();
            event.preventDefault();
          }

          if (event.code === "ArrowDown") {
            index += 1;
          }

          if (event.code === "ArrowUp") {
            index -= 1;
          }

          if (index < 0 || index > data.length - 1) {
            index = 0;
          }

          setActiveItemIndex(index);
        }
      });
    }
  }, [data]);

  useEffect(() => {
    if (debouncedSearchTerm.length > 0) {
      showSearchResultsOverlayPanel();
    } else {
      hideSearchResultsOverlayPanel();
    }
  }, [debouncedSearchTerm]);

  return (
    <div className='searchRoot'>
      <div className='p-input-icon-left ui-fluid searchForm'>
        <i className={classNames("pi pi-search piSearch")} />
        <form ref={searchFormRef} onSubmit={(event: any) => submitSearch(event, data, activeItemIndex)}>
          <InputText
            value={searchKeyword}
            ref={searchInputRef}
            placeholder={t("placeholder") as string}
            onFocus={(e) => e.target.value.length > 0 && showSearchResultsOverlayPanel()}
            onChange={(e) => {
              setSearchKeyword(e.target.value);
            }}
          />
        </form>
        {!isFetching && searchKeyword && (
          <i
            className='pi pi-times searchReset'
            onClick={() => {
              setSearchKeyword("");
              hideSearchResultsOverlayPanel();
              searchInputRef?.current?.focus();
            }}
          ></i>
        )}
        {isFetching && <i className='pi pi-spinner pi-spin searchReset'></i>}
      </div>
      <OverlayPanel
        ref={searchResultsOverlayPanel}
        onShow={() => (overlayPanelActive.current = true)}
        onHide={() => (overlayPanelActive.current = false)}
        className='searchOverlayPanel'
      >
        {!debouncedIsError && data && data.length > 0 && (
          <SearchResultsListing
            data={data}
            activeItemIndex={activeItemIndex}
            submitSearch={(index) => navigateToken(data, index)}
          />
        )}
        {!debouncedIsError && data && data.length === 0 && <SearchResultsEmptyState searchKeyword={searchKeyword} />}
        {(!Boolean(data) || debouncedIsError) && <SearchResultsValidation isError={debouncedIsError} />}
      </OverlayPanel>
    </div>
  );
}

export default memo(AppSearchTokens);
