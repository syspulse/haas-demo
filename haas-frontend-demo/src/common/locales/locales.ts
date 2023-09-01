import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import en from "./en/translation.json";
import ua from "./ua/translation.json";

export const defaultNS = "translation";
export const appLocales = {
  universe: {
    translation: en,
    name: "English",
    key: "universe",
  },
  uk: {
    translation: ua,
    name: "Ukraine",
    key: "uk",
  },
};

i18n.use(initReactI18next).init({
  lng: "universe",
  defaultNS,
  resources: appLocales,
});

export default i18n;
