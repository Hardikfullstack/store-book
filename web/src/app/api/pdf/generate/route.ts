import { NextResponse } from 'next/server';
import { getSession } from '@/lib/session';
import { InvoicePdfGenerator, type PdfSaleData, type PdfCartItem } from '@/lib/InvoicePdfGenerator';

interface GeneratePdfRequestBody {
  sale: PdfSaleData;
  cartItems: PdfCartItem[];
  shopName: string;
  shopAddress: string;
  shopGstin: string;
  invoiceSettings?: {
    accentColor?: string;
    logoUrl?: string;
    headerText?: string;
    footerText?: string;
    signatureUrl?: string;
    showGstBreakdown?: boolean;
    showBankDetails?: boolean;
    upiId?: string;
    bankDetails?: string;
    termsAndConditions?: string;
  };
}

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    let body: GeneratePdfRequestBody;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: 'Invalid JSON payload' }, { status: 400 });
    }

    const { sale, cartItems, shopName, shopAddress, shopGstin, invoiceSettings } = body;

    if (!sale) {
      return NextResponse.json({ error: 'Missing sale data in request body' }, { status: 400 });
    }
    if (!Array.isArray(cartItems)) {
      return NextResponse.json({ error: 'cartItems must be an array' }, { status: 400 });
    }
    if (!shopName) {
      return NextResponse.json({ error: 'Missing shopName in request body' }, { status: 400 });
    }

    try {
      const pdfDataUri = await InvoicePdfGenerator.generateInvoicePdf(
        sale,
        cartItems,
        shopName,
        shopAddress,
        shopGstin || '',
        invoiceSettings
      );

      const base64Data = pdfDataUri.replace(/^data:application\/pdf;base64,/, '');
      const pdfBuffer = Buffer.from(base64Data, 'base64');

      const fileName = `${sale.type === 'ESTIMATE' ? 'Estimate' : 'Invoice'}_${sale.id.substring(0, 8)}.pdf`;

      return new NextResponse(pdfBuffer, {
        headers: {
          'Content-Type': 'application/pdf',
          'Content-Disposition': `inline; filename="${fileName}"`,
          'Content-Length': String(pdfBuffer.length),
        },
      });
    } catch (generationError) {
      console.error('PDF generation failed:', generationError);
      return NextResponse.json(
        { error: 'Failed to generate PDF invoice' },
        { status: 500 }
      );
    }
  } catch (error) {
    console.error('Error in /api/pdf/generate:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
