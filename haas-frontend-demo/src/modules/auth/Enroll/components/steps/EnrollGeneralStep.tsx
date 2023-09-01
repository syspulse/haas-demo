import AppEditProfile from "@app/common/components/AppEditProfile/AppEditProfile";
import { IEnroll } from "@app/common/models/auth";
import { ComponentProps } from "@app/common/models/general";

export default function EnrollGeneralStep({ isActive, onAction }: ComponentProps<any>) {
  if (!isActive) {
    return <></>;
  }

  return (
    <AppEditProfile
      acceptButtonText='Next'
      hideFormName={true}
      formsSet={["general"]}
      onSave={(profile: Partial<IEnroll>) => onAction(profile)}
    />
  );
}
