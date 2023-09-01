import AppEditProfile from "@app/common/components/AppEditProfile/AppEditProfile";
import { IEnroll } from "@app/common/models/auth";
import { usePutUserMutation } from "@app/common/services/user.service";
import { setLanguage } from "@app/common/store/slice/appCommonState";
import { useDispatch } from "react-redux";

export default function Profile() {
  const dispatch = useDispatch()
  const [putUser] = usePutUserMutation();

  return (
    <>
      <h1 className='text-24 mb-20'>Profile Settings</h1>
      <AppEditProfile
        onSave={(profile: Partial<IEnroll>) => {
          putUser({
            email: profile.email,
            avatar: profile.avatar,
            name: profile.name,
          });
          dispatch(setLanguage(profile.locale as string));
        }}
      ></AppEditProfile>
    </>
  );
}
