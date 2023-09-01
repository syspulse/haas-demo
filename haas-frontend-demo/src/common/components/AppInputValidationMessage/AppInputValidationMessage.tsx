export default function AppInputValidationMessage({ field, errors }: any) {
  if (errors && errors.hasOwnProperty(field)) {
    return <small className='p-error formControlError'>{errors[field]?.message}</small>;
  }

  return <></>;
}
