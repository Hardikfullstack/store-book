export function FormattedAmount({ amount, symbol = '₹' }: { amount: number | string | undefined | null, symbol?: string }) {
  const numericAmount = typeof amount === 'number' ? amount : parseFloat(amount as string) || 0;
  
  // Format with exactly two decimals
  const formattedTwoDecimals = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(numericAmount);

  // If a custom symbol was passed (though Intl handles ₹), we can override or just rely on Intl.
  // We'll rely on Intl which handles ₹ by default for INR.
  
  // Full raw amount for hover tooltip
  const fullAmountString = `${symbol}${numericAmount}`;

  return (
    <span title={fullAmountString}>
      {formattedTwoDecimals}
    </span>
  );
}
