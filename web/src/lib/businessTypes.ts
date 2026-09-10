export interface BusinessTypeOption {
  id: string;
  label: string;
  emoji: string;
  description: string;
}

export const BUSINESS_TYPES: readonly BusinessTypeOption[] = [
  {
    id: 'general',
    label: 'General Store',
    emoji: '🏪',
    description: 'All-purpose general retail store with mixed goods',
  },
  {
    id: 'medical',
    label: 'Medical & Pharmacy',
    emoji: '💊',
    description: 'Pharmacies, medical stores, and healthcare supplies',
  },
  {
    id: 'grocery',
    label: 'Grocery & Kirana',
    emoji: '🛒',
    description: 'Daily provisions, food items, and household goods',
  },
  {
    id: 'dairy_sweets',
    label: 'Dairy & Sweets (Mithai)',
    emoji: '🥛',
    description: 'Milk products, sweets, desserts, and snacks',
  },
  {
    id: 'bakery',
    label: 'Bakery & Confectionery',
    emoji: '🍞',
    description: 'Breads, pastries, cakes, biscuits, and confectionery',
  },
  {
    id: 'footwear',
    label: 'Footwear Store',
    emoji: '👟',
    description: 'Shoes, sandals, slippers, and leather accessories',
  },
  {
    id: 'electronics',
    label: 'Electronics & Mobiles',
    emoji: '📱',
    description: 'Mobile phones, accessories, gadgets, and consumer electronics',
  },
  {
    id: 'hardware',
    label: 'Hardware & Tools',
    emoji: '🔨',
    description: 'Construction tools, plumbing, paints, and fasteners',
  },
  {
    id: 'electrical',
    label: 'Electrical & Lighting',
    emoji: '💡',
    description: 'Wiring, switches, lighting fixtures, and electrical appliances',
  },
  {
    id: 'stationery',
    label: 'Books & Stationery',
    emoji: '📚',
    description: 'Office stationery, books, school supplies, and printing',
  },
  {
    id: 'cosmetics',
    label: 'Beauty & Cosmetics',
    emoji: '💄',
    description: 'Makeup, skincare, haircare, and personal grooming products',
  },
] as const;

export type BusinessTypeId = (typeof BUSINESS_TYPES)[number]['id'];

export function getBusinessTypeLabel(businessType?: string | null): string {
  if (!businessType) return 'General Store';
  const found = BUSINESS_TYPES.find((b) => b.id === businessType);
  return found ? found.label : businessType;
}

export function getBusinessTypeEmoji(businessType?: string | null): string {
  if (!businessType) return '🏪';
  const found = BUSINESS_TYPES.find((b) => b.id === businessType);
  return found ? found.emoji : '🏪';
}

export function isValidBusinessType(type: string): boolean {
  return BUSINESS_TYPES.some((b) => b.id === type);
}

