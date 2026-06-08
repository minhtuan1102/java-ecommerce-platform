import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Notice from '../components/Notice';
import { formatMoney, getApiError } from '../utils/format';

const Checkout = () => {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [recipientName, setRecipientName] = useState('');
  const [addressForm, setAddressForm] = useState({
    street: '',
    hamlet: '',
    ward: '',
    province: '',
  });
  const [phoneNumber, setPhoneNumber] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const response = await api.get('/cart');
        if (!active) return;
        setCart(response.data);
        if (!response.data?.items?.length) navigate('/cart');
      } catch (err) {
        if (active) setError(getApiError(err, 'Không thể tải giỏ hàng.'));
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [navigate]);

  const submitCheckout = async (event) => {
    event.preventDefault();
    const shippingAddress = [
      addressForm.street,
      addressForm.hamlet,
      addressForm.ward,
      addressForm.province,
    ]
      .map((part) => part.trim())
      .filter(Boolean)
      .join(', ');

    setSubmitting(true);
    setError('');
    try {
      const response = await api.post('/orders/checkout', { recipientName, shippingAddress, phoneNumber, paymentMethod });
      if (response.data?.paymentUrl) {
        window.location.href = response.data.paymentUrl;
        return;
      }
      navigate('/my-orders');
    } catch (err) {
      setError(getApiError(err, 'Không thể đặt hàng.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <main className="mx-auto max-w-5xl px-4 py-6 text-sm text-gray-500">Đang tải thanh toán...</main>;

  return (
    <main className="mx-auto max-w-5xl px-4 py-6 md:px-6">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-gray-950">Thanh toán</h1>
        <p className="mt-1 text-sm text-gray-500">Nhập thông tin nhận hàng và xác nhận đơn.</p>
      </div>

      {error && <div className="mb-4"><Notice type="error" message={error} /></div>}

      <div className="grid gap-5 lg:grid-cols-[1fr_340px]">
        <form id="checkout-form" onSubmit={submitCheckout} className="rounded-md border border-gray-200 bg-white p-5">
          <h2 className="font-bold text-gray-950">Thông tin nhận hàng</h2>
          <div className="mt-4 space-y-4">
            <input
              required
              value={recipientName}
              onChange={(e) => setRecipientName(e.target.value)}
              placeholder="Tên người nhận"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
            <div className="grid gap-4 md:grid-cols-2">
              <input
                required
                value={addressForm.street}
                onChange={(e) => setAddressForm({ ...addressForm, street: e.target.value })}
                placeholder="Số nhà, tên đường"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              />
              <input
                required
                value={addressForm.hamlet}
                onChange={(e) => setAddressForm({ ...addressForm, hamlet: e.target.value })}
                placeholder="Thôn/ấp/tổ"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              />
              <input
                required
                value={addressForm.ward}
                onChange={(e) => setAddressForm({ ...addressForm, ward: e.target.value })}
                placeholder="Xã/phường/thị trấn"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              />
              <input
                required
                value={addressForm.province}
                onChange={(e) => setAddressForm({ ...addressForm, province: e.target.value })}
                placeholder="Tỉnh/thành phố"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              />
            </div>
            <input required value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="Số điện thoại liên hệ" className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
            <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm">
              <option value="COD">Thanh toán khi nhận hàng (COD)</option>
              <option value="VNPAY">Thanh toán VNPAY</option>
            </select>
          </div>
        </form>

        <aside className="h-fit rounded-md border border-gray-200 bg-white p-5">
          <h2 className="font-bold text-gray-950">Đơn hàng</h2>
          <div className="mt-4 space-y-3">
            {cart?.items?.map((item) => (
              <div key={item.id} className="flex justify-between gap-3 text-sm">
                <div className="flex min-w-0 gap-3">
                  <img src={item.imageUrl || 'https://via.placeholder.com/80'} alt={item.productName} className="h-14 w-14 flex-none rounded-md object-cover" />
                  <div className="min-w-0">
                  <div className="font-medium text-gray-950">{item.productName}</div>
                  <div className="text-xs text-gray-500">x{item.quantity}</div>
                  </div>
                </div>
                <div className="flex-none font-semibold">{formatMoney(item.subtotal)}</div>
              </div>
            ))}
          </div>
          <div className="mt-4 border-t border-gray-100 pt-4 flex justify-between">
            <span className="font-semibold">Tổng thanh toán</span>
            <span className="text-lg font-bold text-red-600">{formatMoney(cart?.totalAmount)}</span>
          </div>
          <button form="checkout-form" disabled={submitting} className="mt-5 w-full rounded-md bg-primary px-4 py-3 text-sm font-semibold text-white hover:bg-primary-dark disabled:opacity-60">
            {submitting ? 'Đang đặt hàng...' : 'Đặt hàng'}
          </button>
          <Link to="/cart" className="mt-3 block text-center text-sm font-semibold text-gray-600">Quay lại giỏ hàng</Link>
        </aside>
      </div>
    </main>
  );
};

export default Checkout;
