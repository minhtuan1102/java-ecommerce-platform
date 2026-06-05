export const formatMoney = (value) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value || 0));

export const getApiError = (error, fallback = 'Không thể thực hiện thao tác. Vui lòng thử lại.') =>
  error?.response?.data?.message || error?.response?.data?.error || fallback;

export const orderStatusLabel = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
};

export const paymentMethodLabel = {
  COD: 'Thanh toán khi nhận hàng',
  ONLINE: 'Thanh toán trực tuyến',
};

export const statusClass = {
  PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
  CONFIRMED: 'bg-blue-50 text-blue-700 border-blue-200',
  SHIPPING: 'bg-cyan-50 text-cyan-700 border-cyan-200',
  DELIVERED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  CANCELLED: 'bg-red-50 text-red-700 border-red-200',
};
