const EmptyState = ({ title, description, action }) => (
  <div className="rounded-md border border-dashed border-gray-300 bg-white p-10 text-center">
    <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
    {description && <p className="mx-auto mt-2 max-w-md text-sm text-gray-500">{description}</p>}
    {action && <div className="mt-5">{action}</div>}
  </div>
);

export default EmptyState;
