import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import { useNavigate } from 'react-router-dom';

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchCart = async () => {
    try {
      const response = await api.get('/cart');
      setCart(response.data);
    } catch (err) {
      console.error('Failed to fetch cart', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      await fetchCart();
    })();
  }, []);

  const handleRemoveItem = async (itemId) => {
    try {
      await api.delete(`/cart/${itemId}`);
      fetchCart(); // Refresh data
    } catch {
      alert('Không thể xóa sản phẩm.');
    }
  };

  const handleCheckout = () => {
    navigate('/checkout');
  };

  if (loading) return <div className="p-8 text-center">Đang tải giỏ hàng...</div>;

  if (!cart || !cart.shops || cart.shops.length === 0) {
    return (
      <div className="max-w-4xl mx-auto mt-10 p-12 bg-white rounded-lg shadow-sm text-center border border-gray-100">
        <div className="text-6xl mb-4">🛒</div>
        <p className="text-gray-500 mb-6">Giỏ hàng của bạn còn trống.</p>
        <button onClick={() => navigate('/')} className="bg-primary text-white px-8 py-2 rounded-md font-bold uppercase">Mua sắm ngay</button>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto p-6 pb-20">
      <div className="flex items-center justify-between mb-12 pt-10">
        <div>
          <h1 className="text-4xl font-black text-dark tracking-tighter uppercase leading-none">Giỏ hàng</h1>
          <p className="text-gray-400 text-[10px] font-black uppercase tracking-[0.2em] mt-2">Xem lại các lựa chọn của bạn</p>
        </div>
        <button onClick={() => navigate('/')} className="text-[10px] font-black text-primary hover:underline uppercase tracking-widest">Tiếp tục mua sắm</button>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-16">
        <div className="lg:col-span-2 space-y-12">
          {cart.shops.map((shop) => (
            <div key={shop.shopId} className="group">
              {/* Shop Header */}
              <div className="flex items-center justify-between mb-6 border-b border-gray-100 pb-4">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-lg bg-dark flex items-center justify-center text-white text-[10px] font-black">
                    {shop.shopName?.[0]}
                  </div>
                  <span className="font-black text-dark uppercase tracking-widest text-[10px]">{shop.shopName}</span>
                </div>
              </div>

              {/* Items */}
              <div className="space-y-8">
                {shop.items.map((item) => (
                  <div key={item.id} className="flex flex-col sm:flex-row items-start sm:items-center gap-8 group/item">
                    <div className="w-24 h-24 bg-gray-50 rounded-[24px] overflow-hidden flex-shrink-0">
                      <img src="https://via.placeholder.com/100" alt={item.productName} className="w-full h-full object-cover grayscale group-hover/item:grayscale-0 transition duration-500" />
                    </div>
                    <div className="flex-grow">
                      <h4 className="text-lg font-black text-dark leading-tight group-hover/item:text-primary transition-colors">{item.productName}</h4>
                      <p className="text-[10px] font-black text-gray-300 uppercase tracking-widest mt-1">Phân loại: {item.tierIndex || 'Mặc định'}</p>
                      <div className="mt-2 text-dark font-black text-base flex items-baseline gap-1">
                        <span className="text-[10px]">₫</span>
                        {item.price?.toLocaleString()}
                        <span className="mx-2 text-gray-100 font-normal">|</span>
                        <span className="text-[10px] text-gray-400 font-bold tracking-normal">Số lượng: {item.quantity}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-8 w-full sm:w-auto justify-between sm:justify-end">
                      <div className="text-right min-w-[120px]">
                        <div className="text-[10px] font-black text-gray-300 uppercase tracking-widest mb-1">Thành tiền</div>
                        <div className="text-dark font-black text-lg">₫{item.subtotal?.toLocaleString()}</div>
                      </div>
                      <button 
                        onClick={() => handleRemoveItem(item.id)} 
                        className="text-gray-300 hover:text-red-500 transition-colors"
                        title="Xóa"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                        </svg>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Tóm tắt đơn hàng */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-[32px] shadow-[0_32px_64px_-16px_rgba(0,0,0,0.08)] p-10 border border-gray-50 sticky top-32">
            <h3 className="text-xl font-black text-dark mb-8 uppercase tracking-tighter">Tóm tắt</h3>
            
            <div className="space-y-6 mb-10">
              <div className="flex justify-between items-center">
                <span className="text-[10px] font-black text-gray-300 uppercase tracking-widest">Tạm tính</span>
                <span className="text-dark font-black">₫{cart.totalAmount?.toLocaleString()}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[10px] font-black text-gray-300 uppercase tracking-widest">Vận chuyển</span>
                <span className="text-accent font-black text-[10px] uppercase tracking-widest">Miễn phí</span>
              </div>
              <div className="h-px bg-gray-50"></div>
              <div className="flex justify-between items-end pt-2">
                <span className="text-dark font-black uppercase tracking-tighter text-sm">Tổng cộng</span>
                <div className="text-right leading-none">
                  <div className="text-3xl font-black text-dark flex items-baseline gap-1 justify-end">
                    <span className="text-sm">₫</span>
                    {cart.totalAmount?.toLocaleString()}
                  </div>
                </div>
              </div>
            </div>

            <button 
              onClick={handleCheckout} 
              className="w-full py-5 bg-dark text-white rounded-2xl font-black uppercase tracking-widest hover:bg-primary transition-all shadow-xl shadow-dark/10 active:scale-[0.98]"
            >
              Đặt hàng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
