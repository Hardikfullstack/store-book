const GST_STATES: Record<string, string> = {
  '01': 'Jammu & Kashmir',
  '02': 'Jammu & Kashmir (Union Territory)',
  '03': 'Himachal Pradesh',
  '04': 'Punjab',
  '05': 'Delhi',
  '06': 'Chandigarh',
  '07': 'Uttarakhand',
  '08': 'Rajasthan',
  '09': 'Haryana',
  '10': 'Chhattisgarh',
  '11': 'Uttar Pradesh (Eastern)',
  '12': 'Uttar Pradesh (Central)',
  '13': 'Uttar Pradesh (Western)',
  '14': 'Uttar Pradesh (Northern)',
  '15': 'Uttar Pradesh (Southern)',
  '16': 'Bihar',
  '17': 'Odisha',
  '18': 'Assam',
  '19': 'West Bengal',
  '20': 'Jharkhand',
  '21': 'Coimbatore (Union Territory)',
  '22': 'Meghalaya',
  '23': 'Manipur',
  '24': 'Nagaland',
  '25': 'Arunachal Pradesh',
  '26': 'Mizoram',
  '27': 'Tripura',
  '28': 'Sikkim',
  '29': 'Gujarat',
  '30': 'Dadra & Nagar Havelia and Daman & Diu',
  '31': 'Maharashtra',
  '32': 'Andhra Pradesh / Telangana',
  '33': 'Karnataka',
  '34': 'Goa',
  '35': 'Kerala',
  '36': 'Tamil Nadu',
  '37': 'Puducherry',
  '38': 'Andaman and Nicobar Islands',
  '39': 'Ladakh (Union Territory)',
};

const GSTIN_WEIGHTS = [1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2];

const DIGIT_MAPS: number[][] = [
  [0],
  [1],
  [2],
  [3],
  [4],
  [5],
  [6],
  [7],
  [8],
  [9],
];

function splitDigits(n: number): number[] {
  if (n < 10) return [n];
  return [Math.floor(n / 10), n % 10];
}

function verifyGSTINChecksum(gstin: string): boolean {
  let sum = 0;
  for (let i = 0; i < 14; i++) {
    const code = gstin.charCodeAt(i) - 48;
    const product = code * GSTIN_WEIGHTS[i];
    sum += splitDigits(product).reduce((a, b) => a + b, 0);
  }
  const checkDigit = (10 - (sum % 10)) % 10;
  const expectedCharCode = checkDigit + 48;
  return gstin.charCodeAt(14) === expectedCharCode || 
         gstin.charCodeAt(14) === (65 + checkDigit - 1);
}

export interface GSTINValidationResult {
  isValid: boolean;
  stateCode: string | null;
  stateName: string | null;
  entityType: string | null;
  errors: string[];
}

export function validateGSTIN(input?: string | null): GSTINValidationResult {
  const errors: string[] = [];
  if (!input || input.trim().length === 0) {
    return {
      isValid: false,
      stateCode: null,
      stateName: null,
      entityType: null,
      errors: ['GSTIN is required'],
    };
  }

  const gstin = input.trim().toUpperCase();

  if (gstin.length !== 15) {
    errors.push('GSTIN must be exactly 15 characters');
    return {
      isValid: false,
      stateCode: null,
      stateName: null,
      entityType: null,
      errors,
    };
  }

  const stateCode = gstin.substring(0, 2);
  const entityCode = gstin.substring(2, 10);
  const entityBranch = gstin.substring(10, 12);
  const entityCheck = gstin.substring(12, 13);

  if (!/^[0-9]{2}$/.test(stateCode)) {
    errors.push('State code must be numeric');
  }

  if (!/^[0-9A-Z]{9}$/.test(entityCode)) {
    errors.push('Entity code must be alphanumeric');
  }

  if (!/^[0-9]{2}$/.test(entityBranch)) {
    errors.push('Entity branch code must be numeric');
  }

  if (!/^[0-9A-Z]$/.test(entityCheck)) {
    errors.push('Entity check character must be alphanumeric');
  }

  if (errors.length > 0) {
    return {
      isValid: false,
      stateCode: null,
      stateName: null,
      entityType: null,
      errors,
    };
  }

  const isValidChecksum = verifyGSTINChecksum(gstin);
  if (!isValidChecksum) {
    errors.push('GSTIN checksum is invalid');
  }

  const stateName = GST_STATES[stateCode] || 'Unknown State';

  let entityType: string | null = null;
  switch (entityCheck.toLowerCase()) {
    case '1':
      entityType = 'Proprietorship';
      break;
    case '2':
      entityType = 'Partnership';
      break;
    case '3':
      entityType = 'Limited Liability Partnership';
      break;
    case '4':
      entityType = 'Private / Public Limited Company';
      break;
    case '5':
      entityType = 'Government Department';
      break;
    case '6':
      entityType = 'Association of Persons';
      break;
    case '7':
      entityType = 'Co-operative Society';
      break;
    case '8':
      entityType = 'Limited Liability Partnership';
      break;
    case '9':
      entityType = 'UT / OIDAR Supplier';
      break;
    default:
      entityType = null;
  }

  return {
    isValid: errors.length === 0,
    stateCode,
    stateName,
    entityType,
    errors,
  };
}

export function getGSTStateName(stateCode: string): string | null {
  return GST_STATES[stateCode] || null;
}

export function isInterstateGSTIN(gstin1?: string | null, gstin2?: string | null): boolean {
  if (!gstin1 || !gstin2) return false;
  const state1 = gstin1.substring(0, 2);
  const state2 = gstin2.substring(0, 2);
  return state1 !== state2;
}
