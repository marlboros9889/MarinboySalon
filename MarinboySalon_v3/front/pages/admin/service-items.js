import { useEffect, useState } from 'react';
import AppLayout from '../../components/AppLayout';
import AdminNavigation from '../../components/AdminNavigation';
import api from '../../api/axios';

const MAX_IMAGE_COUNT = 4;
const emptyForm = { name: '', price: '', durationMinutes: '', description: '', active: true };

/** 시술 정보와 선택한 이미지 파일을 한 화면에서 등록합니다. */
export default function AdminServiceItems() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [selectedImages, setSelectedImages] = useState([]);
  const [savedImageUrls, setSavedImageUrls] = useState([]);
  const [editingItemId, setEditingItemId] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const loadItems = async () => {
    try {
      const response = await api.get('/api/admin/service-items');
      setItems(response.data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || '시술 항목을 불러오지 못했습니다.');
    }
  };

  useEffect(() => { loadItems(); }, []);

  const onChange = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  /** 목록의 메뉴를 폼으로 복사해 등록 화면에서 바로 수정합니다. */
  const onEdit = (item) => {
    setEditingItemId(item.id);
    setForm({
      name: item.name,
      price: String(item.price),
      durationMinutes: String(item.durationMinutes),
      description: item.description || '',
      active: item.active,
    });
    setSavedImageUrls(item.imageUrls || []);
    setSelectedImages([]);
    setError('');
  };

  const cancelEdit = () => {
    setEditingItemId(null);
    setForm(emptyForm);
    setSavedImageUrls([]);
    setSelectedImages([]);
    setError('');
  };

  /** 선택 파일을 최대 네 장으로 제한하고 등록 전 미리보기를 만듭니다. */
  const onImageSelect = (event) => {
    const imageFiles = Array.from(event.target.files || []);
    const invalidFile = imageFiles.find((file) => !['image/jpeg', 'image/png', 'image/webp'].includes(file.type));
    if (imageFiles.length > MAX_IMAGE_COUNT || invalidFile) {
      setError('JPG, PNG, WEBP 이미지를 최대 4장까지 선택할 수 있습니다.');
      return;
    }
    setError('');
    setSelectedImages(imageFiles);
  };

  const removeSelectedImage = (imageIndex) => {
    setSelectedImages(selectedImages.filter((imageFile, index) => index !== imageIndex));
  };

  /** 삭제는 예약 이력을 보존하기 위해 메뉴를 비활성 상태로 바꿉니다. */
  const onDelete = async (item) => {
    if (!window.confirm(`'${item.name}' 메뉴를 비활성화할까요?`)) {
      return;
    }
    try {
      await api.delete(`/api/admin/service-items/${item.id}`);
      if (editingItemId === item.id) {
        cancelEdit();
      }
      await loadItems();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '시술 메뉴 삭제에 실패했습니다.');
    }
  };

  /** 파일 업로드가 성공한 뒤 반환 URL만 메뉴 정보와 함께 저장합니다. */
  const onSubmit = async (event) => {
    event.preventDefault();
    const durationMinutes = Number(form.durationMinutes);
    if (durationMinutes < 30 || durationMinutes % 30 !== 0) {
      setError('소요 시간은 30분, 60분, 90분처럼 30분 단위로 입력해 주세요.');
      return;
    }
    try {
      setError('');
      setUploading(true);
      const imageFormData = new FormData();
      selectedImages.forEach((imageFile) => imageFormData.append('images', imageFile));
      const imageResponse = selectedImages.length > 0
        ? await api.post('/api/admin/service-items/images', imageFormData)
        : { data: [] };
      const imageUrls = selectedImages.length > 0 ? imageResponse.data : savedImageUrls;
      const requestData = {
        ...form,
        price: Number(form.price),
        durationMinutes,
        imageUrls,
      };
      if (editingItemId) {
        await api.put(`/api/admin/service-items/${editingItemId}`, requestData);
      } else {
        await api.post('/api/admin/service-items', requestData);
      }
      cancelEdit();
      await loadItems();
    } catch (requestError) {
      setError(requestError.response?.data?.message || '시술 메뉴 저장에 실패했습니다.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <AppLayout>
      <section className="page-section container">
        <header className="page-heading admin-heading"><p className="eyebrow">ADMIN</p><h1 className="heading-text">시술 메뉴 관리</h1></header>
        <AdminNavigation />
        <form className="admin-inline-form" onSubmit={onSubmit}>
          {editingItemId && <p className="admin-edit-notice">선택한 메뉴를 수정 중입니다. 새 이미지를 선택하면 기존 이미지가 교체됩니다.</p>}
          <label className="admin-field">시술명<input name="name" value={form.name} onChange={onChange} placeholder="예: 디자인 커트" required /></label>
          <label className="admin-field">가격<input name="price" type="number" min="0" value={form.price} onChange={onChange} placeholder="30000" required /></label>
          <label className="admin-field">소요 시간<input name="durationMinutes" type="number" min="30" step="30" value={form.durationMinutes} onChange={onChange} placeholder="30분 단위" required /></label>
          <label className="admin-field">설명<input name="description" value={form.description} onChange={onChange} placeholder="시술 설명" /></label>
          <label className="admin-active-field"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} /> 고객에게 메뉴 표시</label>
          <fieldset className="admin-image-upload">
            <legend>메뉴 이미지 선택 (최대 4장)</legend>
            <label className="image-select-button" htmlFor="service-images">이미지 선택</label>
            <input id="service-images" type="file" accept="image/jpeg,image/png,image/webp" multiple onChange={onImageSelect} />
            <p>JPG, PNG, WEBP / 각 5MB 이하 {editingItemId && '/ 이미지 미선택 시 기존 이미지 유지'}</p>
            {editingItemId && savedImageUrls.length > 0 && selectedImages.length === 0 && <div className="admin-image-thumbnail-list">
              {savedImageUrls.map((imageUrl, imageIndex) => <img key={imageUrl} src={imageUrl.startsWith('/uploads/') ? `http://localhost:8082${imageUrl}` : imageUrl} alt={`${form.name} 기존 이미지 ${imageIndex + 1}`} />)}
            </div>}
            <div className="selected-image-list">
              {selectedImages.map((imageFile, imageIndex) => (
                <div key={`${imageFile.name}-${imageIndex}`} className="selected-image-item">
                  <img src={URL.createObjectURL(imageFile)} alt={`${imageIndex + 1}번 선택 이미지`} />
                  <span>{imageFile.name}</span>
                  <button className="link-button" type="button" onClick={() => removeSelectedImage(imageIndex)}>제거</button>
                </div>
              ))}
            </div>
          </fieldset>
          <div className="admin-form-actions">
            <button className="primary-button" type="submit" disabled={uploading}>{uploading ? '이미지 저장 중...' : (editingItemId ? '메뉴 수정 저장' : '메뉴 등록')}</button>
            {editingItemId && <button className="secondary-button" type="button" onClick={cancelEdit}>수정 취소</button>}
          </div>
        </form>
        {error && <p className="error-message">{error}</p>}
        <div className="service-grid admin-grid">
          {items.map((item) => <article className="paper-card" key={item.id}>
            <h2 className="heading-text">{item.name}</h2>
            <p>{item.price.toLocaleString()}원 · {item.durationMinutes}분</p>
            <p>등록 이미지 {item.imageUrls?.length || 0} / 4장</p>
            <div className="admin-image-thumbnail-list">
              {item.imageUrls?.map((imageUrl, imageIndex) => <img key={imageUrl} src={imageUrl.startsWith('/uploads/') ? `http://localhost:8082${imageUrl}` : imageUrl} alt={`${item.name} 이미지 ${imageIndex + 1}`} />)}
            </div>
            <p>{item.active ? '사용 중' : '비활성'}</p>
            <div className="admin-card-actions">
              <button className="secondary-button" type="button" onClick={() => onEdit(item)}>메뉴 수정</button>
              <button className="danger-button" type="button" onClick={() => onDelete(item)}>메뉴 삭제</button>
            </div>
          </article>)}
        </div>
      </section>
    </AppLayout>
  );
}
