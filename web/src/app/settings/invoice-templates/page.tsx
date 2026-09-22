import { getSession } from '@/lib/session';
import { getDataConnect } from 'firebase-admin/data-connect';
import { redirect } from 'next/navigation';
import TemplateEditorClient from './TemplateEditorClient';
import { InvoiceSettingsState } from '@/store/pdfTemplateSlice';

export default async function InvoiceTemplatesPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  let invoiceSettings: InvoiceSettingsState | null = null;

  try {
    const dc = getDataConnect({
      serviceId: 'store-book',
      location: 'us-central1',
    });

    const result = await dc.executeGraphql(
      `query GetInvoiceSettings($storeId: String!) {
        invoiceSettings(where: { storeId: { eq: $storeId }, isDeleted: { eq: false } }) {
          id
          storeId
          templateStyle
          logoUrl
          accentColor
          headerText
          footerText
          signatureUrl
          showGstBreakdown
          showBankDetails
          upiId
          bankDetails
          termsAndConditions
          isDeleted
          updatedAt
        }
      }`,
      { variables: { storeId: session.storeId } },
    );

    const data = result.data as {
      invoiceSettings?: {
        id: string;
        storeId: string;
        templateStyle?: string;
        logoUrl?: string;
        accentColor?: string;
        headerText?: string;
        footerText?: string;
        signatureUrl?: string;
        showGstBreakdown?: boolean;
        showBankDetails?: boolean;
        upiId?: string;
        bankDetails?: string;
        termsAndConditions?: string;
      }[];
    };

    if (data?.invoiceSettings?.length) {
      const settings = data.invoiceSettings[0];
      invoiceSettings = {
        id: settings.id,
        storeId: settings.storeId,
        templateStyle: (settings.templateStyle ?? 'standard-gst') as InvoiceSettingsState['templateStyle'],
        logoUrl: settings.logoUrl ?? '',
        accentColor: settings.accentColor ?? '#0f766e',
        headerText: settings.headerText ?? 'TAX INVOICE',
        footerText: settings.footerText ?? 'Thank you for your business!',
        signatureUrl: settings.signatureUrl ?? '',
        showGstBreakdown: settings.showGstBreakdown ?? true,
        showBankDetails: settings.showBankDetails ?? false,
        upiId: settings.upiId ?? '',
        bankDetails: settings.bankDetails ?? '',
        termsAndConditions: settings.termsAndConditions ?? '',
        isDraft: false,
      };
    }
  } catch (error) {
    console.error('Failed to fetch invoice settings:', error);
  }

  return <TemplateEditorClient initialSettings={invoiceSettings} />;
}
