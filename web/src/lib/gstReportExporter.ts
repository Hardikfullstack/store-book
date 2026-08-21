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

/**
 * Exports GSTR-2 Inward Supplies (Purchases) report matching Android ExcelExporter.exportGstr2 format.
 */
export function exportGstr2Report(
  purchases: any[],
  purchaseItems: any[],
  suppliersMap: Map<string, any>,
  allItemsMap: Map<string, any>,
  businessGstin: string,
  businessName: string,
  monthName: string,
  year: number
) {
  const purchaseItemsMap = new Map<string, any[]>();
  for (const item of purchaseItems) {
    const pId = String(item.purchaseId ?? item.purchase_id ?? '').trim();
    if (pId) {
      if (!purchaseItemsMap.has(pId)) {
        purchaseItemsMap.set(pId, []);
      }
      purchaseItemsMap.get(pId)!.push(item);
    }
  }

  // Pre-calculate per-purchase taxes
  let sumTaxable = 0;
  let sumCgst = 0;
  let sumSgst = 0;
  let sumIgst = 0;
  let sumTotal = 0;

  const rowsXml: string[] = [];

  for (const purchase of purchases) {
    const pId = String(purchase.id ?? purchase.purchase_id ?? '').trim();
    const items = purchaseItemsMap.get(pId) || [];
    const supplier = suppliersMap?.get(String(purchase.supplierId ?? purchase.supplier_id ?? '').trim());
    const supplierGstin = supplier?.gstin || '';

    const invoiceItems = items.map((pi) => {
      const itemMaster = allItemsMap?.get(String(pi.itemId ?? pi.item_id ?? '').trim());
      const buyPrice = Number(pi.buyPrice ?? pi.buy_price ?? itemMaster?.buyPrice ?? 0);
      return {
        id: String(pi.id ?? ''),
        sell_price: buyPrice, // Inward supplies: buy price acts as base calculation price
        quantity: Number(pi.quantity ?? 1),
        taxRate: Number(itemMaster?.taxRate ?? pi.taxRate ?? pi.tax_rate ?? 0),
      };
    });

    const purchaseTotal = Number(purchase.totalAmount ?? purchase.total_amount) || 0;

    let taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      0, // No global invoice discount for purchase
      businessGstin,
      supplierGstin
    );

    // Fallback if item details were not synced/available for this purchase
    if (invoiceItems.length === 0 || (taxSummary.netTaxableAmount <= 0 && purchaseTotal > 0)) {
      taxSummary = {
        ...taxSummary,
        subTotal: purchaseTotal,
        totalDiscount: 0,
        netTaxableAmount: purchaseTotal,
      };
    }

    const billNo = purchase.billNumber || `PUR${String(purchase.id ?? '').slice(0, 5).toUpperCase()}`;
    const rawTs = Number(purchase.timestamp) || 0;
    const purchaseTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
    const d = new Date(purchaseTs);
    const dateStr = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
    const supplierName = purchase.supplierName ?? purchase.supplier_name ?? supplier?.name ?? 'Supplier';
    const displayGstin = supplierGstin || '-';

    sumTaxable += taxSummary.netTaxableAmount;
    sumCgst += taxSummary.totalCgst;
    sumSgst += taxSummary.totalSgst;
    sumIgst += taxSummary.totalIgst;
    sumTotal += purchaseTotal;

    rowsXml.push(`
      <Row ss:Height="20">
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(dateStr)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(billNo)}</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(supplierName)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(displayGstin)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.netTaxableAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.totalCgst.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.totalSgst.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${taxSummary.totalIgst.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseTotal.toFixed(2)}</Data></Cell>
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
  <Style ss:ID="MetaLabel">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#333333"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="MetaValue">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#111827"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Header">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#9999FF" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
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
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="TotalCurrency">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
    <NumberFormat ss:Format="&quot;₹&quot;#,##0.00"/>

   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Double" ss:Weight="3" ss:Color="#111827"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#111827"/>
   </Borders>
  </Style>
 </Styles>
 <Worksheet ss:Name="GSTR-2 Purchases">
  <Table ss:DefaultRowHeight="18">
   <Column ss:Width="85"/>
   <Column ss:Width="95"/>
   <Column ss:Width="175"/>
   <Column ss:Width="125"/>
   <Column ss:Width="110"/>
   <Column ss:Width="90"/>
   <Column ss:Width="90"/>
   <Column ss:Width="90"/>
   <Column ss:Width="115"/>

   <!-- Row 1: Title -->
   <Row ss:Height="28">
    <Cell ss:StyleID="Title"><Data ss:Type="String">GSTR-2 Inward Supplies (Purchases) Report</Data></Cell>
   </Row>
   <Row ss:Height="10"/>

   <!-- Row 3: Metadata Part 1 -->
   <Row ss:Height="18">
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Business Name:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(businessName)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">GSTIN:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(businessGstin || 'Not Provided')}</Data></Cell>
   </Row>

   <!-- Row 4: Metadata Part 2 -->
   <Row ss:Height="18">
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Generated On:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(generatedDate)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Total Purchases Count:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="Number">${purchases.length}</Data></Cell>
   </Row>
   <Row ss:Height="10"/>

   <!-- Row 6: Table Headers -->
   <Row ss:Height="24">
    <Cell ss:StyleID="Header"><Data ss:Type="String">Date</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Bill No / ID</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Supplier Name</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Supplier GSTIN</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Taxable Value (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">CGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">SGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">IGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Total Amount (₹)</Data></Cell>
   </Row>

   <!-- Row 7+: Data Rows -->
   ${rowsXml.join('')}

   <!-- Totals Row -->
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

  downloadExcelWorkbook(xmlContent, `GSTR2_${monthName}_${year}.xls`);
}

/**
 * Exports GSTR-3B Monthly Consolidated Summary report matching Android ExcelExporter.exportGstr3B format.
 */
export function exportGstr3BReport(
  sales: any[],
  saleItems: any[],
  purchases: any[],
  purchaseItems: any[],
  suppliersMap: Map<string, any>,
  allItemsMap: Map<string, any>,
  businessGstin: string,
  businessName: string,
  monthName: string,
  year: number
) {
  // 1. Group items
  const saleItemsMap = new Map<string, any[]>();
  for (const item of saleItems) {
    const sId = String(item.saleId ?? item.sale_id ?? '').trim();
    if (sId) {
      if (!saleItemsMap.has(sId)) saleItemsMap.set(sId, []);
      saleItemsMap.get(sId)!.push(item);
    }
  }

  const purchaseItemsMap = new Map<string, any[]>();
  for (const item of purchaseItems) {
    const pId = String(item.purchaseId ?? item.purchase_id ?? '').trim();
    if (pId) {
      if (!purchaseItemsMap.has(pId)) purchaseItemsMap.set(pId, []);
      purchaseItemsMap.get(pId)!.push(item);
    }
  }

  // 2. Aggregate Sales Tax
  let saleTaxable = 0;
  let saleCgst = 0;
  let saleSgst = 0;
  let saleIgst = 0;
  let saleTotalTax = 0;

  for (const sale of sales) {
    const sId = String(sale.id ?? sale.sale_id ?? '').trim();
    const items = saleItemsMap.get(sId) || [];
    const invoiceItems = items.map((i) => {
      const itemMaster = allItemsMap?.get(String(i.itemId ?? i.item_id ?? '').trim());
      const sellPrice = Number(i.sellPrice ?? i.sell_price ?? itemMaster?.sellPrice ?? 0);
      return {
        id: String(i.id ?? ''),
        sell_price: sellPrice,
        quantity: Number(i.quantity ?? 1),
        taxRate: Number(itemMaster?.taxRate ?? i.taxRate ?? i.tax_rate ?? 0),
      };
    });

    const saleTotal = Number(sale.totalAmount ?? sale.total_amount) || 0;
    const saleDiscount = Number(sale.discountAmount ?? sale.discount_amount) || 0;

    let taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      saleDiscount,
      businessGstin,
      sale.customerGstin || sale.customer_gstin
    );

    if (invoiceItems.length === 0 || (taxSummary.netTaxableAmount <= 0 && saleTotal > 0)) {
      const fallbackTaxable = Math.max(0, saleTotal - saleDiscount);
      taxSummary = {
        ...taxSummary,
        subTotal: saleTotal,
        totalDiscount: saleDiscount,
        netTaxableAmount: fallbackTaxable,
      };
    }

    saleTaxable += taxSummary.netTaxableAmount;
    saleCgst += taxSummary.totalCgst;
    saleSgst += taxSummary.totalSgst;
    saleIgst += taxSummary.totalIgst;
    saleTotalTax += (taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst);
  }

  // 3. Aggregate Purchases Tax (ITC)
  let purchaseTaxable = 0;
  let purchaseCgst = 0;
  let purchaseSgst = 0;
  let purchaseIgst = 0;
  let purchaseTotalTax = 0;

  for (const purchase of purchases) {
    const pId = String(purchase.id ?? purchase.purchase_id ?? '').trim();
    const items = purchaseItemsMap.get(pId) || [];
    const supplier = suppliersMap?.get(String(purchase.supplierId ?? purchase.supplier_id ?? '').trim());
    const supplierGstin = supplier?.gstin || '';

    const invoiceItems = items.map((pi) => {
      const itemMaster = allItemsMap?.get(String(pi.itemId ?? pi.item_id ?? '').trim());
      const buyPrice = Number(pi.buyPrice ?? pi.buy_price ?? itemMaster?.buyPrice ?? 0);
      return {
        id: String(pi.id ?? ''),
        sell_price: buyPrice,
        quantity: Number(pi.quantity ?? 1),
        taxRate: Number(itemMaster?.taxRate ?? pi.taxRate ?? pi.tax_rate ?? 0),
      };
    });

    const purchaseTotal = Number(purchase.totalAmount ?? purchase.total_amount) || 0;

    let taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      0,
      businessGstin,
      supplierGstin
    );

    if (invoiceItems.length === 0 || (taxSummary.netTaxableAmount <= 0 && purchaseTotal > 0)) {
      taxSummary = {
        ...taxSummary,
        subTotal: purchaseTotal,
        totalDiscount: 0,
        netTaxableAmount: purchaseTotal,
      };
    }

    purchaseTaxable += taxSummary.netTaxableAmount;
    purchaseCgst += taxSummary.totalCgst;
    purchaseSgst += taxSummary.totalSgst;
    purchaseIgst += taxSummary.totalIgst;
    purchaseTotalTax += (taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst);
  }

  const now = new Date();
  const generatedDate = `${String(now.getDate()).padStart(2, '0')}/${String(now.getMonth() + 1).padStart(2, '0')}/${now.getFullYear()}`;

  const netCgst = saleCgst - purchaseCgst;
  const netSgst = saleSgst - purchaseSgst;
  const netIgst = saleIgst - purchaseIgst;
  const netTotalTax = saleTotalTax - purchaseTotalTax;

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
  <Style ss:ID="SectionHeader">
   <Font ss:FontName="Calibri" ss:Size="12" ss:Bold="1" ss:Color="#1A237E"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="MetaLabel">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#333333"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="MetaValue">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#111827"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Header">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#9999FF" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
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
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="TotalCurrency">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="&quot;₹&quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Double" ss:Weight="3" ss:Color="#111827"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#111827"/>
   </Borders>
  </Style>
 </Styles>
 <Worksheet ss:Name="GSTR-3B Summary">
  <Table ss:DefaultRowHeight="18">
   <Column ss:Width="260"/>
   <Column ss:Width="130"/>
   <Column ss:Width="120"/>
   <Column ss:Width="120"/>
   <Column ss:Width="120"/>
   <Column ss:Width="140"/>

   <!-- Row 1: Title -->
   <Row ss:Height="28">
    <Cell ss:StyleID="Title"><Data ss:Type="String">GSTR-3B Monthly Consolidated Summary</Data></Cell>
   </Row>
   <Row ss:Height="10"/>

   <!-- Row 3: Metadata Part 1 -->
   <Row ss:Height="18">
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Business Name:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(businessName)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">GSTIN:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(businessGstin || 'Not Provided')}</Data></Cell>
   </Row>

   <!-- Row 4: Metadata Part 2 -->
   <Row ss:Height="18">
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Generated On:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(generatedDate)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
   </Row>
   <Row ss:Height="14"/>

   <!-- SECTION 1: Outward Supplies -->
   <Row ss:Height="22">
    <Cell ss:StyleID="SectionHeader"><Data ss:Type="String">1. OUTWARD SUPPLIES (SALES LIABILITY)</Data></Cell>
   </Row>
   <Row ss:Height="24">
    <Cell ss:StyleID="Header"><Data ss:Type="String">Outward Supplies Type</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Taxable Value (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">CGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">SGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">IGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Total Tax (₹)</Data></Cell>
   </Row>
   <Row ss:Height="20">
    <Cell ss:StyleID="DataLeft"><Data ss:Type="String">Standard Rated Local &amp; Interstate Supplies</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleTaxable.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleCgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleSgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleIgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleTotalTax.toFixed(2)}</Data></Cell>
   </Row>
   <Row ss:Height="14"/>

   <!-- SECTION 2: Inward Supplies -->
   <Row ss:Height="22">
    <Cell ss:StyleID="SectionHeader"><Data ss:Type="String">2. INWARD SUPPLIES (ELIGIBLE ITC FROM PURCHASES)</Data></Cell>
   </Row>
   <Row ss:Height="24">
    <Cell ss:StyleID="Header"><Data ss:Type="String">Inward Supplies Type</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Taxable Value (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">CGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">SGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">IGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Total Tax (₹)</Data></Cell>
   </Row>
   <Row ss:Height="20">
    <Cell ss:StyleID="DataLeft"><Data ss:Type="String">Inward Supplies Eligible For Input Tax Credit</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseTaxable.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseCgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseSgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseIgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseTotalTax.toFixed(2)}</Data></Cell>
   </Row>
   <Row ss:Height="14"/>

   <!-- SECTION 3: Net Tax Payable -->
   <Row ss:Height="22">
    <Cell ss:StyleID="SectionHeader"><Data ss:Type="String">3. NET TAX PAYABLE / (REMAINING ITC)</Data></Cell>
   </Row>
   <Row ss:Height="24">
    <Cell ss:StyleID="Header"><Data ss:Type="String">Tax Component</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Outward Tax (A) (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">ITC Available (B) (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Net Tax Payable (A - B) (₹)</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
   </Row>
   <Row ss:Height="20">
    <Cell ss:StyleID="DataLeft"><Data ss:Type="String">CGST</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleCgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseCgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${netCgst.toFixed(2)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
   </Row>
   <Row ss:Height="20">
    <Cell ss:StyleID="DataLeft"><Data ss:Type="String">SGST</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleSgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseSgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${netSgst.toFixed(2)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
   </Row>
   <Row ss:Height="20">
    <Cell ss:StyleID="DataLeft"><Data ss:Type="String">IGST</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleIgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseIgst.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${netIgst.toFixed(2)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
   </Row>
   <Row ss:Height="22">
    <Cell ss:StyleID="TotalLabel"><Data ss:Type="String">Total Net Tax</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${saleTotalTax.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${purchaseTotalTax.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="TotalCurrency"><Data ss:Type="Number">${netTotalTax.toFixed(2)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
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

  downloadExcelWorkbook(xmlContent, `GSTR3B_${monthName}_${year}.xls`);
}

/**
 * Exports Transaction-wise GST Detailed Breakup matching Android ExcelExporter.exportGstDetailed format.
 */
export function exportDetailedGstReport(
  sales: any[],
  saleItems: any[],
  purchases: any[],
  purchaseItems: any[],
  suppliersMap: Map<string, any>,
  allItemsMap: Map<string, any>,
  businessGstin: string,
  businessName: string,
  monthName: string,
  year: number
) {
  // 1. Group line items
  const saleItemsMap = new Map<string, any[]>();
  for (const item of saleItems) {
    const sId = String(item.saleId ?? item.sale_id ?? '').trim();
    if (sId) {
      if (!saleItemsMap.has(sId)) saleItemsMap.set(sId, []);
      saleItemsMap.get(sId)!.push(item);
    }
  }

  const purchaseItemsMap = new Map<string, any[]>();
  for (const item of purchaseItems) {
    const pId = String(item.purchaseId ?? item.purchase_id ?? '').trim();
    if (pId) {
      if (!purchaseItemsMap.has(pId)) purchaseItemsMap.set(pId, []);
      purchaseItemsMap.get(pId)!.push(item);
    }
  }

  let sumQty = 0;
  let sumTaxable = 0;
  let sumCgst = 0;
  let sumSgst = 0;
  let sumIgst = 0;
  let sumTotal = 0;

  const rowsXml: string[] = [];

  // 2. Process Sales Rows
  for (const sale of sales) {
    const sId = String(sale.id ?? sale.sale_id ?? '').trim();
    const items = saleItemsMap.get(sId) || [];
    const invoiceItems = items.map((i) => {
      const itemMaster = allItemsMap?.get(String(i.itemId ?? i.item_id ?? '').trim());
      const sellPrice = Number(i.sellPrice ?? i.sell_price ?? itemMaster?.sellPrice ?? 0);
      return {
        id: String(i.id ?? ''),
        sell_price: sellPrice,
        quantity: Number(i.quantity ?? 1),
        taxRate: Number(itemMaster?.taxRate ?? i.taxRate ?? i.tax_rate ?? 0),
        unit: i.unit || itemMaster?.unit || 'Units',
        hsnCode: itemMaster?.hsnCode || i.hsnCode || i.hsn_code || '-',
        name: itemMaster?.name || i.itemName || i.item_name || 'Item',
      };
    });

    const saleTotal = Number(sale.totalAmount ?? sale.total_amount) || 0;
    const saleDiscount = Number(sale.discountAmount ?? sale.discount_amount) || 0;
    const customerGstin = sale.customerGstin || sale.customer_gstin || '';
    const partyName = sale.customerName || sale.customer_name || 'Cash / B2C Customer';
    const txnId = sale.invoiceNumber || `INV${sId.slice(0, 5).toUpperCase()}`;

    const rawTs = Number(sale.timestamp) || 0;
    const saleTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
    const d = new Date(saleTs);
    const dateStr = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;

    const taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      saleDiscount,
      businessGstin,
      customerGstin
    );

    if (invoiceItems.length === 0) {
      const fallbackTaxable = Math.max(0, saleTotal - saleDiscount);
      sumTaxable += fallbackTaxable;
      sumTotal += saleTotal;

      rowsXml.push(`
      <Row ss:Height="20">
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(dateStr)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(txnId)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">Sale</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(partyName)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(customerGstin || '-')}</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">General Sale</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">-</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">0</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">1</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">Unit</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${fallbackTaxable.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">0.00</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">0.00</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">0.00</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${saleTotal.toFixed(2)}</Data></Cell>
      </Row>`);
    } else {
      taxSummary.itemDetails.forEach((detail, idx) => {
        const rawItem = invoiceItems[idx];
        const qty = rawItem?.quantity || 1;
        const unit = rawItem?.unit || 'Units';
        const hsn = rawItem?.hsnCode || '-';
        const name = rawItem?.name || 'Item';
        const rate = rawItem?.taxRate || 0;

        sumQty += qty;
        sumTaxable += detail.netAmountBeforeTax;
        sumCgst += detail.cgstAmount;
        sumSgst += detail.sgstAmount;
        sumIgst += detail.igstAmount;
        sumTotal += detail.totalAmountWithTax;

        rowsXml.push(`
      <Row ss:Height="20">
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(dateStr)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(txnId)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">Sale</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(partyName)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(customerGstin || '-')}</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(name)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(hsn)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">${rate}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">${qty}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(unit)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.netAmountBeforeTax.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.cgstAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.sgstAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.igstAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.totalAmountWithTax.toFixed(2)}</Data></Cell>
      </Row>`);
      });
    }
  }

  // 3. Process Purchase Rows
  for (const purchase of purchases) {
    const pId = String(purchase.id ?? purchase.purchase_id ?? '').trim();
    const items = purchaseItemsMap.get(pId) || [];
    const supplier = suppliersMap?.get(String(purchase.supplierId ?? purchase.supplier_id ?? '').trim());
    const supplierGstin = supplier?.gstin || '';
    const supplierName = purchase.supplierName ?? purchase.supplier_name ?? supplier?.name ?? 'Supplier';
    const txnId = purchase.billNumber || `PUR${pId.slice(0, 5).toUpperCase()}`;

    const rawTs = Number(purchase.timestamp) || 0;
    const purchaseTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
    const d = new Date(purchaseTs);
    const dateStr = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;

    const invoiceItems = items.map((pi) => {
      const itemMaster = allItemsMap?.get(String(pi.itemId ?? pi.item_id ?? '').trim());
      const buyPrice = Number(pi.buyPrice ?? pi.buy_price ?? itemMaster?.buyPrice ?? 0);
      return {
        id: String(pi.id ?? ''),
        sell_price: buyPrice,
        quantity: Number(pi.quantity ?? 1),
        taxRate: Number(itemMaster?.taxRate ?? pi.taxRate ?? pi.tax_rate ?? 0),
        unit: pi.unit || itemMaster?.unit || 'Units',
        hsnCode: itemMaster?.hsnCode || pi.hsnCode || pi.hsn_code || '-',
        name: itemMaster?.name || pi.itemName || pi.item_name || 'Item',
      };
    });

    const purchaseTotal = Number(purchase.totalAmount ?? purchase.total_amount) || 0;

    const taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      0,
      businessGstin,
      supplierGstin
    );

    if (invoiceItems.length === 0) {
      sumTaxable += purchaseTotal;
      sumTotal += purchaseTotal;

      rowsXml.push(`
      <Row ss:Height="20">
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(dateStr)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(txnId)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">Purchase</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(supplierName)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(supplierGstin || '-')}</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">General Purchase</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">-</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">0</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">1</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">Unit</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseTotal.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">0.00</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">0.00</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">0.00</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${purchaseTotal.toFixed(2)}</Data></Cell>
      </Row>`);
    } else {
      taxSummary.itemDetails.forEach((detail, idx) => {
        const rawItem = invoiceItems[idx];
        const qty = rawItem?.quantity || 1;
        const unit = rawItem?.unit || 'Units';
        const hsn = rawItem?.hsnCode || '-';
        const name = rawItem?.name || 'Item';
        const rate = rawItem?.taxRate || 0;

        sumQty += qty;
        sumTaxable += detail.netAmountBeforeTax;
        sumCgst += detail.cgstAmount;
        sumSgst += detail.sgstAmount;
        sumIgst += detail.igstAmount;
        sumTotal += detail.totalAmountWithTax;

        rowsXml.push(`
      <Row ss:Height="20">
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(dateStr)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(txnId)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">Purchase</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(supplierName)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(supplierGstin || '-')}</Data></Cell>
        <Cell ss:StyleID="DataLeft"><Data ss:Type="String">${escapeXml(name)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(hsn)}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">${rate}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="Number">${qty}</Data></Cell>
        <Cell ss:StyleID="DataCenter"><Data ss:Type="String">${escapeXml(unit)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.netAmountBeforeTax.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.cgstAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.sgstAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.igstAmount.toFixed(2)}</Data></Cell>
        <Cell ss:StyleID="DataCurrency"><Data ss:Type="Number">${detail.totalAmountWithTax.toFixed(2)}</Data></Cell>
      </Row>`);
      });
    }
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
  <Style ss:ID="MetaLabel">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#333333"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="MetaValue">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#111827"/>
   <Alignment ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="Header">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#9999FF" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#D1D5DB"/>
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
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="TotalCurrency">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="&quot;₹&quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Double" ss:Weight="3" ss:Color="#111827"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#111827"/>
   </Borders>
  </Style>
  <Style ss:ID="TotalCenter">
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#111827"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Double" ss:Weight="3" ss:Color="#111827"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#111827"/>
   </Borders>
  </Style>
 </Styles>
 <Worksheet ss:Name="GST Detailed Breakup">
  <Table ss:DefaultRowHeight="18">
   <Column ss:Width="85"/>
   <Column ss:Width="95"/>
   <Column ss:Width="75"/>
   <Column ss:Width="160"/>
   <Column ss:Width="125"/>
   <Column ss:Width="160"/>
   <Column ss:Width="85"/>
   <Column ss:Width="85"/>
   <Column ss:Width="65"/>
   <Column ss:Width="65"/>
   <Column ss:Width="115"/>
   <Column ss:Width="90"/>
   <Column ss:Width="90"/>
   <Column ss:Width="90"/>
   <Column ss:Width="115"/>

   <!-- Row 1: Title -->
   <Row ss:Height="28">
    <Cell ss:StyleID="Title"><Data ss:Type="String">Transaction-wise GST Detailed Breakup</Data></Cell>
   </Row>
   <Row ss:Height="10"/>

   <!-- Row 3: Metadata Part 1 -->
   <Row ss:Height="18">
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Business Name:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(businessName)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">GSTIN:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(businessGstin || 'Not Provided')}</Data></Cell>
   </Row>

   <!-- Row 4: Metadata Part 2 -->
   <Row ss:Height="18">
    <Cell ss:StyleID="MetaLabel"><Data ss:Type="String">Generated On:</Data></Cell>
    <Cell ss:StyleID="MetaValue"><Data ss:Type="String">${escapeXml(generatedDate)}</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
   </Row>
   <Row ss:Height="10"/>

   <!-- Row 6: Table Headers (15 Columns matching Android) -->
   <Row ss:Height="24">
    <Cell ss:StyleID="Header"><Data ss:Type="String">Date</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Txn ID</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Type</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Party Name</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Party GSTIN</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Item Name</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">HSN Code</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Tax Rate (%)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Qty</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Unit</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Taxable Value (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">CGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">SGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">IGST (₹)</Data></Cell>
    <Cell ss:StyleID="Header"><Data ss:Type="String">Total Amount (₹)</Data></Cell>
   </Row>

   <!-- Row 7+: Data Rows -->
   ${rowsXml.join('')}

   <!-- Totals Row -->
   <Row ss:Height="22">
    <Cell ss:StyleID="TotalLabel"><Data ss:Type="String">Total</Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell><Data ss:Type="String"></Data></Cell>
    <Cell ss:StyleID="TotalCenter"><Data ss:Type="Number">${sumQty}</Data></Cell>
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

  downloadExcelWorkbook(xmlContent, `Detailed_GST_${monthName}_${year}.xls`);
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
