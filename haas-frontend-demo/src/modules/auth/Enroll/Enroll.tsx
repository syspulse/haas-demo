import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { IEnroll } from "@app/common/models/auth";
import { useAuthMutation } from "@app/common/services/auth";
import {
  useCreateUserMutation,
  useGetEmailCodeMutation,
  useVerifyEmailCodeMutation,
} from "@app/common/services/enroll";
import { RootState } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import { Steps } from "primereact/steps";
import { useEffect, useState } from "react";

import EnrollConfirmEmailStep from "./components/steps/EnrollConfirmEmailStep";
import EnrollFinishStep from "./components/steps/EnrollFinishStep";
import EnrollGeneralStep from "./components/steps/EnrollGeneralStep";

const statusToTabIndex = (phase: string) => {
  let index = 0;

  phase && (index = 1);
  phase === "START" && (index = 0);
  phase === "CONFIRM_EMAIL_ACK" && (index = 2);

  return index;
};

function AuthEnroll() {
  const [createUser, createUserParams] = useCreateUserMutation();
  const [getEmail, getEmailParams] = useGetEmailCodeMutation();
  const [verifyEmailCode, verifyEmailCodeParams] = useVerifyEmailCodeMutation();
  const [auth, { isLoading }] = useAuthMutation();

  const enroll: IEnroll = useAppSelector((state: RootState) => state.common.enroll);
  const [activeIndex, setActiveIndex] = useState(statusToTabIndex(enroll.phase));
  const items = [{ label: "General" }, { label: "Confirm email" }, { label: "Welcome" }];

  useEffect(() => {
    if (enroll.phase) {
      setActiveIndex(statusToTabIndex(enroll.phase));
    }

    if (enroll.phase === "START_ACK") {
      getEmail({ id: enroll.id, email: enroll.email, name: enroll.name });
    }
  }, [enroll, getEmail]);

  return (
    <div className='enrollRoot'>
      <Steps activeIndex={activeIndex} model={items} />
      <div className='enrollStep p-card'>
        <AppLoadingError
          isError={false}
          isFetching={
            createUserParams.isLoading || getEmailParams.isLoading || verifyEmailCodeParams.isLoading || isLoading
          }
        />
        <EnrollGeneralStep isActive={activeIndex === 0} onAction={(profile: IEnroll) => createUser(profile)} />
        <EnrollConfirmEmailStep
          isActive={activeIndex === 1}
          onAction={(data: any) => verifyEmailCode({ code: data.code, id: enroll.id })}
        />
        <EnrollFinishStep isActive={activeIndex === 2} onAction={() => auth(enroll.provider)} />
      </div>
    </div>
  );
}

export default AuthEnroll;
