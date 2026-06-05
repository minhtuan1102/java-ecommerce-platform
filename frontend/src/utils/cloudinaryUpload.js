import api from '../api/axios';
import { getApiError } from './format';

export const openSignedUploadWidget = ({ folder, multiple = false, maxFiles = 1, onSuccess, setNotice }) => {
  if (!window.cloudinary) {
    setNotice?.({ type: 'error', text: 'Cloudinary Upload Widget chưa được tải.' });
    return;
  }

  const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
  const apiKey = import.meta.env.VITE_CLOUDINARY_API_KEY;

  if (!cloudName || !apiKey) {
    setNotice?.({ type: 'error', text: 'Thiếu VITE_CLOUDINARY_CLOUD_NAME hoặc VITE_CLOUDINARY_API_KEY trong frontend .env.' });
    return;
  }

  const widget = window.cloudinary.createUploadWidget(
    {
      cloudName,
      apiKey,
      folder,
      sources: ['local'],
      multiple,
      maxFiles,
      clientAllowedFormats: ['jpg', 'jpeg', 'png', 'webp'],
      maxFileSize: 5_000_000,
      uploadSignature: async (callback, paramsToSign) => {
        try {
          const response = await api.post('/admin/cloudinary/signature', { paramsToSign });
          callback(response.data.signature);
        } catch (err) {
          setNotice?.({ type: 'error', text: getApiError(err, 'Không thể ký upload Cloudinary.') });
        }
      },
    },
    (error, result) => {
      if (error) {
        setNotice?.({ type: 'error', text: 'Upload ảnh thất bại.' });
        return;
      }

      if (result?.event === 'success') {
        onSuccess?.(result.info);
      }
    }
  );

  widget.open();
};
