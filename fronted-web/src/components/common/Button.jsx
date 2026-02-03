export default function Button({ 
  children, 
  variant = 'primary', 
  size = 'md',
  onClick, 
  type = 'button',
  disabled = false,
  className = '',
  icon: Icon
}) {
  const baseStyles = 'font-medium rounded-lg transition-all duration-200 inline-flex items-center justify-center gap-2';
  
  const variants = {
    primary: 'bg-epn-blue text-white hover:bg-opacity-90 disabled:bg-gray-300',
    secondary: 'bg-gray-200 text-gray-800 hover:bg-gray-300 disabled:bg-gray-100',
    danger: 'bg-epn-red text-white hover:bg-opacity-90 disabled:bg-gray-300',
    success: 'bg-green-600 text-white hover:bg-green-700 disabled:bg-gray-300',
    outline: 'border-2 border-epn-blue text-epn-blue hover:bg-epn-blue hover:text-white disabled:border-gray-300 disabled:text-gray-300',
  };
  
  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2',
    lg: 'px-6 py-3 text-lg',
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${className} disabled:cursor-not-allowed`}
    >
      {Icon && <Icon className="w-4 h-4" />}
      {children}
    </button>
  );
}