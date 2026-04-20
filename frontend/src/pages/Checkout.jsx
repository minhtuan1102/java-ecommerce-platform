import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import { useNavigate } from 'react-router-dom';

const Checkout = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [shippingAddress, setShippingAddress] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCart = async () => {
      try {
        const response = await api.get('/cart');
        setCart(response.data);
        if (!response.data || !response.data.shops || response.data.shops.length === 0) {
          navigate('/cart'); // Redirect to cart if empty
        }
      } catch (err) {
        console.error('Failed to fetch cart', err);
      } finally {
        setLoading(false);
      }
    };
    fetchCart();
  }, [navigate]);

  const handleCheckout = async (e) => {
    e.preventDefault();
    if (!shippingAddress.trim() || !phoneNumber.trim()) {
      alert('Vui lòng nhập đầy đủ thông tin nhận hàng.');
      return;
    }

    setSubmitting(true);
    try {
      await api.post('/orders/checkout', { shippingAddress, phoneNumber, paymentMethod });
      alert('Đặt hàng thành công!');
      navigate('/my-orders');
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi đặt hàng.');
      setSubmitting(false);
    }
  };

  if (loading) return <div className="p-8 text-center">Đang tải thông tin...</div>;

  let totalAmount = 0;
  if (cart && cart.shops) {
      cart.shops.forEach(shop => {
          shop.items.forEach(item => {
              totalAmount += (item.price * item.quantity);
          });
      });
  }

  return (
    <div className="max-w-4xl mx-auto p-6 pb-20">
      <div className="mb-10">
        <h1 className="text-3xl font-black text-gray-800 tracking-tight uppercase">Thanh toán</h1>
        <div className="h-1.5 w-12 bg-primary mt-2 rounded-full"></div>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div>
            <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 mb-6">
                <h2 className="text-xl font-black mb-6 uppercase">Thông tin nhận hàng</h2>
                <form id="checkout-form" onSubmit={handleCheckout} className="space-y-4">
                    <div>
                        <label className="block text-sm font-bold text-gray-700 mb-1">Địa chỉ nhận hàng <span className="text-red-500">*</span></label>
                        <input 
                            type="text" 
                            required
                            value={shippingAddress} 
                            onChange={e => setShippingAddress(e.target.value)} 
                            placeholder="Số nhà, Tên đường, Xã/Phường, Quận/Huyện, Tỉnh/Thành phố"
                            className="w-full px-4 py-3 rounded-xl border-gray-200 bg-gray-50 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-gray-700 mb-1">Số điện thoại <span className="text-red-500">*</span></label>
                        <input 
                            type="tel" 
                            required
                            value={phoneNumber} 
                            onChange={e => setPhoneNumber(e.target.value)} 
                            placeholder="Số điện thoại liên hệ"
                            className="w-full px-4 py-3 rounded-xl border-gray-200 bg-gray-50 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-gray-700 mb-1">Phương thức thanh toán <span className="text-red-500">*</span></label>
                        <select
                            value={paymentMethod}
                            onChange={(e) => setPaymentMethod(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border-gray-200 bg-gray-50 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                        >
                            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
                        </select>
                    </div>
                </form>
            </div>
        </div>

        <div>
            <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-black mb-6 uppercase">Tóm tắt đơn hàng</h2>
                <div className="space-y-4 mb-6 max-h-60 overflow-y-auto pr-2">
                    {cart?.shops?.map(shop => (
                        <div key={shop.shopId} className="border-b border-gray-50 pb-4 last:border-0">
                            <h3 className="font-bold text-sm text-gray-800 mb-2 uppercase">{shop.shopName}</h3>
                            {shop.items.map(item => (
                                <div key={item.id} className="flex justify-between items-center text-sm mb-2">
                                    <div className="flex-1 pr-4">
                                        <p className="line-clamp-1 font-medium">{item.productName}</p>
                                        <p className="text-xs text-gray-500">x{item.quantity}</p>
                                    </div>
                                    <div className="font-black text-primary">
                                        ₫{(item.price * item.quantity).toLocaleString()}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
                
                <div className="border-t border-gray-100 pt-4">
                    <div className="flex justify-between items-center text-lg font-black">
                        <span>Tổng thanh toán:</span>
                        <span className="text-primary text-2xl">₫{totalAmount.toLocaleString()}</span>
                    </div>
                </div>

                <button 
                    type="submit" 
                    form="checkout-form"
                    disabled={submitting}
                    className="w-full mt-8 bg-primary hover:bg-primary-dark text-white font-black py-4 rounded-xl shadow-lg shadow-primary/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed uppercase tracking-widest"
                >
                    {submitting ? 'Đang xử lý...' : 'Đặt hàng ngay'}
                </button>
            </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
