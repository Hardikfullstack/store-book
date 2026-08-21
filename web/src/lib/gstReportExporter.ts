import { BillingEngine } from '@/lib/BillingEngine';
import { DcSale, DcSaleItem, DcItem } from '@/types/dataconnect';

interface InvoiceTaxItem {
  id: string;
  sell_price: number;
  quantity: number;
  taxRate?: number;
}

/**
 * Exports GSTR-1 Outward Supplies report matching the exact Android POS Excel format and visual styling.
 */
export function exportGstr1Report(
  sales: DcSale[],
  saleItems: DcSaleItem[],
  allItemsMap: Map<string, DcItem>,
  businessGstin: string,
  businessName: string,
  monthName: string,
  year: number
) {
  const saleItemsMap = new Map<string, DcSaleItem[]>();
  for (const item of saleItems) {
    if (!saleItemsMap.has(item.saleId)) {
      saleItemsMap.set(item.saleId, []);
    }
    saleItemsMap.get(item.saleId)!.push(item);
  }

  // Pre-calculate per-sale taxes
  let sumTaxable = 0;
  let sumCgst = 0;
  let sumSgst = 0;
  let sumIgst = 0;
  let sumTotal = 0;

  const rowsXml: string[] = [];

  for (const sale of sales) {
    const items = saleItemsMap.get(sale.id) || [];
    const invoiceItems: InvoiceTaxItem[] = items.map((i) => ({
      id: i.id,
      sell_price: i.sellPrice,
      quantity: i.quantity,
      taxRate: 0,
    }));

    const taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      sale.discountAmount || 0,
      businessGstin,
      sale.customerGstin
    );

    const invoiceNo = sale.invoiceNumber || `INV${sale.id.slice(0, 5).toUpperCase()}`;
    const rawTs = Number(sale.timestamp) || 0;
    const saleTs = rawTs > 10000000000 ? rawTs : rawTs * 1000;
    const d = new Date(saleTs);
    const dateStr = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
    const customerName = sale.customerName || 'Cash / Anonymous';
    const customerGstin = sale.customerGstin || '-';

    sumTaxable += taxSummary.netTaxableAmount;
    sumCgst += taxSummary.totalCgst;
    sumSgst += taxSummary.totalSgst;
    sumIgst += taxSummary.totalIgst;
    sumTotal += sale.totalAmount;

    rowsXml.push(`
      <Row ss:Height="20">
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(dateStr)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(invoiceNo)}</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(customerName)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(customerGstin)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.netTaxableAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.totalCgst.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.totalSgst.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.totalIgst.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${sale.totalAmount.toFixed(2)}</Data></Cell>
      </Row>`);
  }

  const now = new Date();
  const generatedDate = `${String(now.getDate()).padStart(2, '0')}/${String(now.getMonth() + 1).padStart(2, '0')}/${now.getFullYear()}`;

  const xmlContent = `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office">
  <Author>StoreBook</Author>
  <Created>${now.toISOString()}</Created>
 </DocumentProperties>
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#000000"/>
  </Style>
  <Style ss:ID="Title">
   <Font ss:FontName="Calibri" ss:Size="16" ss:Bold="1" ss:Color="#1A237E"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="BoldMeta">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Header">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#9999FF" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#6B6BE5"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#6B6BE5"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#6B6BE5"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#6B6BE5"/>
   </Borders>
  </Style>
  <Style ss:ID="DataCenter">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
   </Borders>
  </Style>
  <Style ss:ID="DataLeft">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
   </Borders>
  </Style>
  <Style ss:ID="DataCurrency">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="&quot;₹&quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E5E7EB"/>
   </Borders>
  </Style>
  <Style ss:ID="TotalLabel">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#000000"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="TotalCurrency">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#000000"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="&quot;₹&quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#000000"/>
    <Border ss:Position="Bottom" ss:LineStyle="Double" ss:Weight="3" ss:Color="#000000"/>
   </Borders>
  </Style>
 </Styles>
 <Worksheet ss:Name="GSTR-1 Sales">
  <Table ss:DefaultRowHeight="18">
   <Column ss:Width="72"/>
   <Column ss:Width="72"/>
   <Column ss:Width="130"/>
   <Column ss:Width="95"/>
   <Column ss:Width="95"/>
   <Column ss:Width="65"/>
   <Column ss:Width="65"/>
   <Column ss:Width="65"/>
   <Column ss:Width="95"/>

   <!-- Row 1: Title Block -->
   <Row ss:Height="30">
    <Cell ss:MergeAcross="8" ss:StyleID="Title"><Data ss:Type="String">GSTR-1 Outward Supplies (Sales) Report</Data></Cell>
   </Row>

   <!-- Row 2: Spacer -->
   <Row ss:Height="12"/>

   <!-- Row 3: Metadata Row 1 -->
   <Row ss:Height="20">
    <Cell ss:StyleID="BoldMeta"><Data ss:Type="String">Business Name:</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(businessName)}</Data></Cell>
    <Cell ss:Index="4" ss:StyleID="BoldMeta"><Data ss:Type="String">GSTIN:</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(businessGstin || 'Not Provided')}</Data></Cell>
   </Row>

   <!-- Row 4: Metadata Row 2 -->
   <Row ss:Height="20">
    <Cell ss:StyleID="BoldMeta"><Data ss:Type="String">Generated On:</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(generatedDate)}</Data></Cell>
    <Cell ss:Index="4" ss:StyleID="BoldMeta"><Data ss:Type="String">Total Sales Count:</Data></Cell>
    <Cell><Data ss:Type="Number">${sales.length}</Data></Cell>
   </Row>

   <!-- Row 5: Spacer -->
   <Row ss:Height="12"/>

   <!-- Row 6: Table Headers -->
   <Row ss:Height="25">
    <Cell ss:StyleID="Header"><Data ss:Type="String">Date</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Invoice No</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Customer Name</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Customer GSTIN</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Taxable Value (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">CGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">SGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">IGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Total Amount (₹)</Data></Cell>
   </Row>

   <!-- Row 7+: Data Rows -->
   ${rowsXml.join('')}

   <!-- Totals Row (Only last 5 currency columns have the double bottom line) -->
   <Row ss:Height="22">
    <Cell ss:StyleID="TotalLabel"><Data ss:Type="String">Total</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${sumTaxable.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${sumCgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${sumSgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${sumIgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${sumTotal.toFixed(2)}</Data></Cell>
   </Row>
  </Table>
  <WorksheetOptions xmlns="urn:schemas-microsoft-com:office:excel">
   <Selected/>
   <Panes>
    <Pane>
     <Number>3</Number>
     <ActiveRow>0</ActiveRow>
     <ActiveCol>0</ActiveCol>
    </Pane>
   </Panes>
   <ProtectObjects>False</ProtectObjects>
   <ProtectScenarios>False</ProtectScenarios>
  </WorksheetOptions>
 </Worksheet>
</Workbook>`;

  downloadExcelWorkbook(xmlContent, `GSTR1_${monthName}_${year}.xls`);
}

function downloadExcelWorkbook(xmlContent: string, fileName: string) {
  const blob = new Blob([xmlContent], { type: 'application/vnd.ms-excel;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function escapeXml(val: unknown): string {
  if (val === null || val === undefined) return '';
  const str = String(val);
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}
