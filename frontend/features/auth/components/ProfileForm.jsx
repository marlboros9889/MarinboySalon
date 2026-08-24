import { authApi } from '../authApi';
import { editableContactValue } from '../profileRules';

/** 예약·내 예약·관리자 화면이 같은 고객 정보 저장 흐름을 재사용합니다. */
export function ProfileForm({
  user,
  title,
  description,
  submitLabel = '정보 저장',
  successMessage,
  failureMessage = '고객 정보 수정에 실패했습니다.',
  dialog = false,
  onSaved,
  onCancel,
  onMessage,
}) {
  const saveProfile = async (event) => {
    event.preventDefault();
    const profile = Object.fromEntries(new FormData(event.currentTarget));
    try {
      const updatedUser = await authApi.updateProfile(profile);
      onSaved(updatedUser);
      onMessage(successMessage);
    } catch (error) {
      onMessage(error.message || failureMessage);
    }
  };

  const form = (
    <form className={`simple-form${dialog ? ' dialog' : ''}`} onSubmit={saveProfile}>
      {onCancel && <button type="button" className="close" onClick={onCancel}>×</button>}
      <h2>{title}</h2>
      {description && <p>{description}</p>}
      <input name="name" defaultValue={user.name} placeholder="이름" required />
      <input
        name="email"
        type="email"
        defaultValue={editableContactValue(user.email, 'email')}
        placeholder="이메일"
        required
      />
      <input
        name="phone"
        defaultValue={editableContactValue(user.phone, 'phone')}
        placeholder="연락처"
        required
      />
      <button>{submitLabel}</button>
    </form>
  );

  return dialog ? <div className="dialog-backdrop">{form}</div> : form;
}
