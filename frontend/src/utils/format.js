export const formatMoney = (value) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value || 0));

export const getApiError = (error, fallback = 'Không thể thực hiện thao tác. Vui lòng thử lại.') =>
  error?.response?.data?.message || error?.response?.data?.error || fallback;

export const orderStatusLabel = {
  PENDING_PAYMENT: 'Chờ thanh toán',
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
};

export const paymentMethodLabel = {
  COD: 'Thanh toán khi nhận hàng',
  VNPAY: 'Thanh toán VNPAY',
  ONLINE: 'Thanh toán trực tuyến',
};

export const paymentStatusLabel = {
  UNPAID: 'Chưa thanh toán',
  PENDING: 'Đang chờ thanh toán',
  PAID: 'Đã thanh toán',
  FAILED: 'Thanh toán lỗi',
  EXPIRED: 'Đã quá hạn',
  REFUND_PENDING: 'Chờ hoàn tiền',
  REFUNDED: 'Đã hoàn tiền',
  COD_PENDING: 'Chờ thu COD',
  COD_COLLECTED: 'Đã thu COD',
};

export const statusClass = {
  PENDING_PAYMENT: 'bg-purple-50 text-purple-700 border-purple-200',
  PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
  CONFIRMED: 'bg-blue-50 text-blue-700 border-blue-200',
  SHIPPING: 'bg-cyan-50 text-cyan-700 border-cyan-200',
  DELIVERED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  CANCELLED: 'bg-red-50 text-red-700 border-red-200',
};
