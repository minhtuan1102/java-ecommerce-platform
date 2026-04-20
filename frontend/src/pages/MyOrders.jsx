import React, { useEffect, useState } from 'react';
import api from '../api/axios';

const MyOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await api.get('/orders/my-orders');
        setOrders(response.data);
      } catch (err) {
        console.error('Failed to fetch orders', err);
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  }, []);

  if (loading) return <div className="p-8 text-center text-gray-500 italic">Đang tải lịch sử đơn hàng...</div>;

  return (
    <div className="max-w-4xl mx-auto p-6 pb-20">
      <div className="mb-10">
        <h1 className="text-3xl font-black text-gray-800 tracking-tight uppercase">Đơn mua của tôi</h1>
        <div className="h-1.5 w-12 bg-primary mt-2 rounded-full"></div>
      </div>
      
      {orders.length === 0 ? (
        <div className="bg-white p-20 text-center rounded-3xl shadow-sm border-2 border-dashed border-gray-100 flex flex-col items-center">
          <div className="text-6xl mb-4 grayscale opacity-20">📦</div>
          <h3 className="text-xl font-bold text-gray-400">Chưa có đơn hàng nào</h3>
          <p className="text-gray-300 text-sm mt-2 max-w-xs">Hãy khám phá các sản phẩm tuyệt vời của E-Market ngay!</p>
        </div>
      ) : (
        <div className="space-y-8">
          {orders.map((order) => (
            <div key={order.id} className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-all">
              <div className="bg-gray-50/50 px-8 py-4 border-b border-gray-100 flex justify-between items-center">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center text-primary text-[10px] font-black uppercase">
                    {order.shopName?.[0]}
                  </div>
                  <span className="font-black text-gray-800 text-sm uppercase tracking-tight">{order.shopName}</span>
                </div>
                <div className={`text-[10px] font-black px-4 py-1.5 rounded-full uppercase tracking-widest shadow-sm ${
                  order.status === 'DELIVERED' ? 'bg-green-500 text-white' : 
                  order.status === 'CANCELLED' ? 'bg-red-500 text-white' : 'bg-orange-400 text-white'
                }`}>
                  {order.status}
                </div>
              </div>
              
              <div className="p-8">
                {order.items.map((item) => (
                  <div key={item.id} className="flex gap-6 mb-6 last:mb-0 items-center group">
                    <div className="w-16 h-16 bg-gray-50 rounded-2xl border border-gray-100 flex-shrink-0 overflow-hidden group-hover:border-primary/20 transition-all">
                      <img src="https://via.placeholder.com/64" alt={item.productName} className="w-full h-full object-cover group-hover:scale-110 transition duration-500" />
                    </div>
                    <div className="flex-grow">
                      <h4 className="text-sm font-black text-gray-800 line-clamp-1 group-hover:text-primary transition">{item.productName}</h4>
                      <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mt-1">
                        Phân loại: {item.tierIndex || 'Mặc định'} <span className="mx-2 text-gray-200">|</span> Số lượng: {item.quantity}
                      </p>
                    </div>
                    <div className="text-lg font-black text-primary flex items-baseline gap-0.5">
                      <span className="text-[10px]">₫</span>
                      {item.price?.toLocaleString()}
                    </div>
                  </div>
                ))}
                
                <div className="mt-8 pt-6 border-t border-gray-50 flex justify-between items-end">
                  <div className="bg-gray-50 px-4 py-1 rounded-full border border-gray-100 text-[10px] font-black text-gray-300 uppercase tracking-tighter">
                    Mã đơn: #{order.id}
                  </div>
                  <div className="text-right">
                    <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest mr-3 block mb-1">Tổng số tiền</span>
                    <div className="text-3xl font-black text-primary leading-none flex items-baseline gap-1 justify-end">
                      <span className="text-sm">₫</span>
                      {order.totalAmount?.toLocaleString()}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyOrders;
