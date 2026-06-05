const AdminLayout = ({ title, description, action, children }) => (
  <main className="mx-auto max-w-7xl px-4 py-6 md:px-6">
    <section>
      <div className="mb-5 rounded-xl border border-gray-200 bg-white/90 p-5 shadow-sm">
        <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-950">{title}</h1>
            {description && <p className="mt-1 text-sm text-gray-500">{description}</p>}
          </div>
          {action}
        </div>
      </div>
      {children}
    </section>
  </main>
);

export default AdminLayout;
