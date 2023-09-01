import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { defaultNS } from "@app/common/locales/locales";
import { useAuthMutation } from "@app/common/services/auth";
import { authProviderItem, getAuthProviders } from "@app/common/services/auth.providers";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { useTranslation } from "react-i18next";

function AuthLogin() {
  const { t } = useTranslation(defaultNS, { keyPrefix: "auth.login" });
  const [auth, { isLoading }] = useAuthMutation();
  const authProviders: authProviderItem[] = getAuthProviders(auth);

  return (
    <Card className='authForm'>
      <AppLoadingError isError={false} isFetching={isLoading} />
      <h1 className='text-24 mb-16'>{t("header.title")}</h1>
      <p className='text-14 text-w400 text--gray-800 mb-24'>{t("header.caption")}</p>
      <div className='authProviders'>
        {authProviders.map((option: authProviderItem, index: number) => (
          <Button
            key={index}
            className='p-button-outlined p-button-secondary p-center'
            onClick={() => option.action(option.key)}
          >
            {option.logo}
            {t(`providers.${option.key}`)}
          </Button>
        ))}
        <p className='text-12 text-w400 text--gray-600 mt-16'>{t("tos")}</p>
      </div>
    </Card>
  );
}

export default AuthLogin;
