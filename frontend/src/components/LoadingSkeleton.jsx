const LoadingSkeleton = ({ rows = 4 }) => (
  <div className="space-y-3">
    {Array.from({ length: rows }).map((_, index) => (
      <div key={index} className="h-24 animate-pulse rounded-md bg-gray-100" />
    ))}
  </div>
);

export default LoadingSkeleton;
