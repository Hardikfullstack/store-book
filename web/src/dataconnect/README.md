# Table of Contents
- [**Overview**](#generated-typescript-readme)
- [**Accessing the connector**](#accessing-the-connector)
  - [*Connecting to the local Emulator*](#connecting-to-the-local-emulator)
- [**Queries**](#queries)
  - [*SyncItems*](#syncitems)
  - [*SyncSales*](#syncsales)
  - [*SyncSaleItems*](#syncsaleitems)
  - [*GetActiveItems*](#getactiveitems)
  - [*SyncUdhaars*](#syncudhaars)
  - [*SyncExpenses*](#syncexpenses)
  - [*GetActiveSales*](#getactivesales)
  - [*GetActiveSaleItems*](#getactivesaleitems)
  - [*GetActiveUdhaars*](#getactiveudhaars)
  - [*GetActiveExpenses*](#getactiveexpenses)
  - [*GetActiveSuppliers*](#getactivesuppliers)
  - [*GetUser*](#getuser)
- [**Mutations**](#mutations)
  - [*SyncItem*](#syncitem)
  - [*SyncSale*](#syncsale)
  - [*SyncSaleItem*](#syncsaleitem)
  - [*SoftDeleteItem*](#softdeleteitem)
  - [*SoftDeleteSale*](#softdeletesale)
  - [*SyncUser*](#syncuser)
  - [*UpdateUser*](#updateuser)
  - [*SyncStore*](#syncstore)
  - [*UpdateStore*](#updatestore)
  - [*SyncUdhaar*](#syncudhaar)
  - [*SoftDeleteUdhaar*](#softdeleteudhaar)
  - [*SyncExpense*](#syncexpense)
  - [*SoftDeleteExpense*](#softdeleteexpense)
  - [*SyncSupplier*](#syncsupplier)

# Generated TypeScript README
This README will guide you through the process of using the generated TypeScript SDK package for the connector `storebook-connector`. It will also provide examples on how to use your generated SDK to call your Data Connect queries and mutations.

***NOTE:** This README is generated alongside the generated SDK. If you make changes to this file, they will be overwritten when the SDK is regenerated.*

You can use this generated SDK by importing from the package `@storebook/dataconnect` as shown below. Both CommonJS and ESM imports are supported.

You can also follow the instructions from the [Data Connect documentation](https://firebase.google.com/docs/data-connect/web-sdk#set-client).

# Accessing the connector
A connector is a collection of Queries and Mutations. One SDK is generated for each connector - this SDK is generated for the connector `storebook-connector`.

You can find more information about connectors in the [Data Connect documentation](https://firebase.google.com/docs/data-connect#how-does).

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig } from '@storebook/dataconnect';

const dataConnect = getDataConnect(connectorConfig);
```

## Connecting to the local Emulator
By default, the connector will connect to the production service.

To connect to the emulator, you can use the following code.
You can also follow the emulator instructions from the [Data Connect documentation](https://firebase.google.com/docs/data-connect/web-sdk#instrument-clients).

```javascript
import { connectDataConnectEmulator, getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig } from '@storebook/dataconnect';

const dataConnect = getDataConnect(connectorConfig);
connectDataConnectEmulator(dataConnect, 'localhost', 9399);
```

After it's initialized, you can call your Data Connect [queries](#queries) and [mutations](#mutations) from your generated SDK.

# Queries

There are two ways to execute a Data Connect Query using the generated Web SDK:
- Using a Query Reference function, which returns a `QueryRef`
  - The `QueryRef` can be used as an argument to `executeQuery()`, which will execute the Query and return a `QueryPromise`
- Using an action shortcut function, which returns a `QueryPromise`
  - Calling the action shortcut function will execute the Query and return a `QueryPromise`

The following is true for both the action shortcut function and the `QueryRef` function:
- The `QueryPromise` returned will resolve to the result of the Query once it has finished executing
- If the Query accepts arguments, both the action shortcut function and the `QueryRef` function accept a single argument: an object that contains all the required variables (and the optional variables) for the Query
- Both functions can be called with or without passing in a `DataConnect` instance as an argument. If no `DataConnect` argument is passed in, then the generated SDK will call `getDataConnect(connectorConfig)` behind the scenes for you.

Below are examples of how to use the `storebook-connector` connector's generated functions to execute each query. You can also follow the examples from the [Data Connect documentation](https://firebase.google.com/docs/data-connect/web-sdk#using-queries).

## SyncItems
You can execute the `SyncItems` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncItems(vars: SyncItemsVariables): QueryPromise<SyncItemsData, SyncItemsVariables>;

syncItemsRef(vars: SyncItemsVariables): QueryRef<SyncItemsData, SyncItemsVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
syncItems(dc: DataConnect, vars: SyncItemsVariables): QueryPromise<SyncItemsData, SyncItemsVariables>;

syncItemsRef(dc: DataConnect, vars: SyncItemsVariables): QueryRef<SyncItemsData, SyncItemsVariables>;
```

### Variables
The `SyncItems` query requires an argument of type `SyncItemsVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncItemsVariables {
  storeId: string;
  lastSync: number;
}
```
### Return Type
Recall that executing the `SyncItems` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncItemsData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncItemsData {
  items: ({
    id: string;
    name: string;
    quantity: number;
    unit: string;
    buyPrice: number;
    sellPrice: number;
    lowStockThreshold: number;
    category: string;
    photoPath?: string | null;
    hsnCode?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & Item_Key)[];
}
```
### Using `SyncItems`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncItems, SyncItemsVariables } from '@storebook/dataconnect';

// The `SyncItems` query requires an argument of type `SyncItemsVariables`:
const syncItemsVars: SyncItemsVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncItems()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncItems(syncItemsVars);
// Variables can be defined inline as well.
const { data } = await syncItems({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncItems(dataConnect, syncItemsVars);

console.log(data.items);

// Or, you can use the `Promise` API.
syncItems(syncItemsVars).then((response) => {
  const data = response.data;
  console.log(data.items);
});
```

### Using `SyncItems`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, syncItemsRef, SyncItemsVariables } from '@storebook/dataconnect';

// The `SyncItems` query requires an argument of type `SyncItemsVariables`:
const syncItemsVars: SyncItemsVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncItemsRef()` function to get a reference to the query.
const ref = syncItemsRef(syncItemsVars);
// Variables can be defined inline as well.
const ref = syncItemsRef({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncItemsRef(dataConnect, syncItemsVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.items);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.items);
});
```

## SyncSales
You can execute the `SyncSales` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncSales(vars: SyncSalesVariables): QueryPromise<SyncSalesData, SyncSalesVariables>;

syncSalesRef(vars: SyncSalesVariables): QueryRef<SyncSalesData, SyncSalesVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
syncSales(dc: DataConnect, vars: SyncSalesVariables): QueryPromise<SyncSalesData, SyncSalesVariables>;

syncSalesRef(dc: DataConnect, vars: SyncSalesVariables): QueryRef<SyncSalesData, SyncSalesVariables>;
```

### Variables
The `SyncSales` query requires an argument of type `SyncSalesVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncSalesVariables {
  storeId: string;
  lastSync: number;
}
```
### Return Type
Recall that executing the `SyncSales` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncSalesData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncSalesData {
  sales: ({
    id: string;
    timestamp: number;
    totalAmount: number;
    discountAmount: number;
    customerName?: string | null;
    customerGstin?: string | null;
    businessGstin?: string | null;
    customerAddress?: string | null;
    businessAddress?: string | null;
    type: string;
    isDeleted: boolean;
    updatedAt: number;
  } & Sale_Key)[];
}
```
### Using `SyncSales`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncSales, SyncSalesVariables } from '@storebook/dataconnect';

// The `SyncSales` query requires an argument of type `SyncSalesVariables`:
const syncSalesVars: SyncSalesVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncSales()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncSales(syncSalesVars);
// Variables can be defined inline as well.
const { data } = await syncSales({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncSales(dataConnect, syncSalesVars);

console.log(data.sales);

// Or, you can use the `Promise` API.
syncSales(syncSalesVars).then((response) => {
  const data = response.data;
  console.log(data.sales);
});
```

### Using `SyncSales`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, syncSalesRef, SyncSalesVariables } from '@storebook/dataconnect';

// The `SyncSales` query requires an argument of type `SyncSalesVariables`:
const syncSalesVars: SyncSalesVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncSalesRef()` function to get a reference to the query.
const ref = syncSalesRef(syncSalesVars);
// Variables can be defined inline as well.
const ref = syncSalesRef({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncSalesRef(dataConnect, syncSalesVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.sales);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.sales);
});
```

## SyncSaleItems
You can execute the `SyncSaleItems` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncSaleItems(vars: SyncSaleItemsVariables): QueryPromise<SyncSaleItemsData, SyncSaleItemsVariables>;

syncSaleItemsRef(vars: SyncSaleItemsVariables): QueryRef<SyncSaleItemsData, SyncSaleItemsVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
syncSaleItems(dc: DataConnect, vars: SyncSaleItemsVariables): QueryPromise<SyncSaleItemsData, SyncSaleItemsVariables>;

syncSaleItemsRef(dc: DataConnect, vars: SyncSaleItemsVariables): QueryRef<SyncSaleItemsData, SyncSaleItemsVariables>;
```

### Variables
The `SyncSaleItems` query requires an argument of type `SyncSaleItemsVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncSaleItemsVariables {
  storeId: string;
  lastSync: number;
}
```
### Return Type
Recall that executing the `SyncSaleItems` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncSaleItemsData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncSaleItemsData {
  saleItemDetails: ({
    id: string;
    saleId: string;
    itemId: string;
    itemName: string;
    quantity: number;
    unit: string;
    sellPrice: number;
    buyPrice: number;
    isDeleted: boolean;
    updatedAt: number;
  } & SaleItemDetail_Key)[];
}
```
### Using `SyncSaleItems`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncSaleItems, SyncSaleItemsVariables } from '@storebook/dataconnect';

// The `SyncSaleItems` query requires an argument of type `SyncSaleItemsVariables`:
const syncSaleItemsVars: SyncSaleItemsVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncSaleItems()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncSaleItems(syncSaleItemsVars);
// Variables can be defined inline as well.
const { data } = await syncSaleItems({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncSaleItems(dataConnect, syncSaleItemsVars);

console.log(data.saleItemDetails);

// Or, you can use the `Promise` API.
syncSaleItems(syncSaleItemsVars).then((response) => {
  const data = response.data;
  console.log(data.saleItemDetails);
});
```

### Using `SyncSaleItems`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, syncSaleItemsRef, SyncSaleItemsVariables } from '@storebook/dataconnect';

// The `SyncSaleItems` query requires an argument of type `SyncSaleItemsVariables`:
const syncSaleItemsVars: SyncSaleItemsVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncSaleItemsRef()` function to get a reference to the query.
const ref = syncSaleItemsRef(syncSaleItemsVars);
// Variables can be defined inline as well.
const ref = syncSaleItemsRef({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncSaleItemsRef(dataConnect, syncSaleItemsVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.saleItemDetails);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.saleItemDetails);
});
```

## GetActiveItems
You can execute the `GetActiveItems` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getActiveItems(vars: GetActiveItemsVariables): QueryPromise<GetActiveItemsData, GetActiveItemsVariables>;

getActiveItemsRef(vars: GetActiveItemsVariables): QueryRef<GetActiveItemsData, GetActiveItemsVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getActiveItems(dc: DataConnect, vars: GetActiveItemsVariables): QueryPromise<GetActiveItemsData, GetActiveItemsVariables>;

getActiveItemsRef(dc: DataConnect, vars: GetActiveItemsVariables): QueryRef<GetActiveItemsData, GetActiveItemsVariables>;
```

### Variables
The `GetActiveItems` query requires an argument of type `GetActiveItemsVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetActiveItemsVariables {
  storeId: string;
}
```
### Return Type
Recall that executing the `GetActiveItems` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetActiveItemsData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetActiveItemsData {
  items: ({
    id: string;
    name: string;
    quantity: number;
    sellPrice: number;
    buyPrice: number;
    category: string;
  } & Item_Key)[];
}
```
### Using `GetActiveItems`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getActiveItems, GetActiveItemsVariables } from '@storebook/dataconnect';

// The `GetActiveItems` query requires an argument of type `GetActiveItemsVariables`:
const getActiveItemsVars: GetActiveItemsVariables = {
  storeId: ..., 
};

// Call the `getActiveItems()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getActiveItems(getActiveItemsVars);
// Variables can be defined inline as well.
const { data } = await getActiveItems({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getActiveItems(dataConnect, getActiveItemsVars);

console.log(data.items);

// Or, you can use the `Promise` API.
getActiveItems(getActiveItemsVars).then((response) => {
  const data = response.data;
  console.log(data.items);
});
```

### Using `GetActiveItems`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getActiveItemsRef, GetActiveItemsVariables } from '@storebook/dataconnect';

// The `GetActiveItems` query requires an argument of type `GetActiveItemsVariables`:
const getActiveItemsVars: GetActiveItemsVariables = {
  storeId: ..., 
};

// Call the `getActiveItemsRef()` function to get a reference to the query.
const ref = getActiveItemsRef(getActiveItemsVars);
// Variables can be defined inline as well.
const ref = getActiveItemsRef({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getActiveItemsRef(dataConnect, getActiveItemsVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.items);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.items);
});
```

## SyncUdhaars
You can execute the `SyncUdhaars` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncUdhaars(vars: SyncUdhaarsVariables): QueryPromise<SyncUdhaarsData, SyncUdhaarsVariables>;

syncUdhaarsRef(vars: SyncUdhaarsVariables): QueryRef<SyncUdhaarsData, SyncUdhaarsVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
syncUdhaars(dc: DataConnect, vars: SyncUdhaarsVariables): QueryPromise<SyncUdhaarsData, SyncUdhaarsVariables>;

syncUdhaarsRef(dc: DataConnect, vars: SyncUdhaarsVariables): QueryRef<SyncUdhaarsData, SyncUdhaarsVariables>;
```

### Variables
The `SyncUdhaars` query requires an argument of type `SyncUdhaarsVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncUdhaarsVariables {
  storeId: string;
  lastSync: number;
}
```
### Return Type
Recall that executing the `SyncUdhaars` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncUdhaarsData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncUdhaarsData {
  udhaarEntries: ({
    id: string;
    storeId: string;
    customerName: string;
    amount: number;
    type: string;
    timestamp: number;
    notes?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & UdhaarEntry_Key)[];
}
```
### Using `SyncUdhaars`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncUdhaars, SyncUdhaarsVariables } from '@storebook/dataconnect';

// The `SyncUdhaars` query requires an argument of type `SyncUdhaarsVariables`:
const syncUdhaarsVars: SyncUdhaarsVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncUdhaars()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncUdhaars(syncUdhaarsVars);
// Variables can be defined inline as well.
const { data } = await syncUdhaars({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncUdhaars(dataConnect, syncUdhaarsVars);

console.log(data.udhaarEntries);

// Or, you can use the `Promise` API.
syncUdhaars(syncUdhaarsVars).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntries);
});
```

### Using `SyncUdhaars`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, syncUdhaarsRef, SyncUdhaarsVariables } from '@storebook/dataconnect';

// The `SyncUdhaars` query requires an argument of type `SyncUdhaarsVariables`:
const syncUdhaarsVars: SyncUdhaarsVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncUdhaarsRef()` function to get a reference to the query.
const ref = syncUdhaarsRef(syncUdhaarsVars);
// Variables can be defined inline as well.
const ref = syncUdhaarsRef({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncUdhaarsRef(dataConnect, syncUdhaarsVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.udhaarEntries);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntries);
});
```

## SyncExpenses
You can execute the `SyncExpenses` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncExpenses(vars: SyncExpensesVariables): QueryPromise<SyncExpensesData, SyncExpensesVariables>;

syncExpensesRef(vars: SyncExpensesVariables): QueryRef<SyncExpensesData, SyncExpensesVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
syncExpenses(dc: DataConnect, vars: SyncExpensesVariables): QueryPromise<SyncExpensesData, SyncExpensesVariables>;

syncExpensesRef(dc: DataConnect, vars: SyncExpensesVariables): QueryRef<SyncExpensesData, SyncExpensesVariables>;
```

### Variables
The `SyncExpenses` query requires an argument of type `SyncExpensesVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncExpensesVariables {
  storeId: string;
  lastSync: number;
}
```
### Return Type
Recall that executing the `SyncExpenses` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncExpensesData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncExpensesData {
  expenseEntries: ({
    id: string;
    storeId: string;
    type: string;
    description: string;
    amount: number;
    timestamp: number;
    supplierName?: string | null;
    supplierPhone?: string | null;
    isDeleted: boolean;
    updatedAt: number;
  } & ExpenseEntry_Key)[];
}
```
### Using `SyncExpenses`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncExpenses, SyncExpensesVariables } from '@storebook/dataconnect';

// The `SyncExpenses` query requires an argument of type `SyncExpensesVariables`:
const syncExpensesVars: SyncExpensesVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncExpenses()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncExpenses(syncExpensesVars);
// Variables can be defined inline as well.
const { data } = await syncExpenses({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncExpenses(dataConnect, syncExpensesVars);

console.log(data.expenseEntries);

// Or, you can use the `Promise` API.
syncExpenses(syncExpensesVars).then((response) => {
  const data = response.data;
  console.log(data.expenseEntries);
});
```

### Using `SyncExpenses`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, syncExpensesRef, SyncExpensesVariables } from '@storebook/dataconnect';

// The `SyncExpenses` query requires an argument of type `SyncExpensesVariables`:
const syncExpensesVars: SyncExpensesVariables = {
  storeId: ..., 
  lastSync: ..., 
};

// Call the `syncExpensesRef()` function to get a reference to the query.
const ref = syncExpensesRef(syncExpensesVars);
// Variables can be defined inline as well.
const ref = syncExpensesRef({ storeId: ..., lastSync: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncExpensesRef(dataConnect, syncExpensesVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.expenseEntries);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.expenseEntries);
});
```

## GetActiveSales
You can execute the `GetActiveSales` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getActiveSales(vars: GetActiveSalesVariables): QueryPromise<GetActiveSalesData, GetActiveSalesVariables>;

getActiveSalesRef(vars: GetActiveSalesVariables): QueryRef<GetActiveSalesData, GetActiveSalesVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getActiveSales(dc: DataConnect, vars: GetActiveSalesVariables): QueryPromise<GetActiveSalesData, GetActiveSalesVariables>;

getActiveSalesRef(dc: DataConnect, vars: GetActiveSalesVariables): QueryRef<GetActiveSalesData, GetActiveSalesVariables>;
```

### Variables
The `GetActiveSales` query requires an argument of type `GetActiveSalesVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetActiveSalesVariables {
  storeId: string;
}
```
### Return Type
Recall that executing the `GetActiveSales` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetActiveSalesData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetActiveSalesData {
  sales: ({
    id: string;
    timestamp: number;
    totalAmount: number;
    discountAmount: number;
    customerName?: string | null;
    type: string;
    notes?: string | null;
    updatedAt: number;
  } & Sale_Key)[];
}
```
### Using `GetActiveSales`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getActiveSales, GetActiveSalesVariables } from '@storebook/dataconnect';

// The `GetActiveSales` query requires an argument of type `GetActiveSalesVariables`:
const getActiveSalesVars: GetActiveSalesVariables = {
  storeId: ..., 
};

// Call the `getActiveSales()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getActiveSales(getActiveSalesVars);
// Variables can be defined inline as well.
const { data } = await getActiveSales({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getActiveSales(dataConnect, getActiveSalesVars);

console.log(data.sales);

// Or, you can use the `Promise` API.
getActiveSales(getActiveSalesVars).then((response) => {
  const data = response.data;
  console.log(data.sales);
});
```

### Using `GetActiveSales`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getActiveSalesRef, GetActiveSalesVariables } from '@storebook/dataconnect';

// The `GetActiveSales` query requires an argument of type `GetActiveSalesVariables`:
const getActiveSalesVars: GetActiveSalesVariables = {
  storeId: ..., 
};

// Call the `getActiveSalesRef()` function to get a reference to the query.
const ref = getActiveSalesRef(getActiveSalesVars);
// Variables can be defined inline as well.
const ref = getActiveSalesRef({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getActiveSalesRef(dataConnect, getActiveSalesVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.sales);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.sales);
});
```

## GetActiveSaleItems
You can execute the `GetActiveSaleItems` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getActiveSaleItems(vars: GetActiveSaleItemsVariables): QueryPromise<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;

getActiveSaleItemsRef(vars: GetActiveSaleItemsVariables): QueryRef<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getActiveSaleItems(dc: DataConnect, vars: GetActiveSaleItemsVariables): QueryPromise<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;

getActiveSaleItemsRef(dc: DataConnect, vars: GetActiveSaleItemsVariables): QueryRef<GetActiveSaleItemsData, GetActiveSaleItemsVariables>;
```

### Variables
The `GetActiveSaleItems` query requires an argument of type `GetActiveSaleItemsVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetActiveSaleItemsVariables {
  storeId: string;
}
```
### Return Type
Recall that executing the `GetActiveSaleItems` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetActiveSaleItemsData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetActiveSaleItemsData {
  saleItemDetails: ({
    id: string;
    saleId: string;
    itemId: string;
    itemName: string;
    quantity: number;
    sellPrice: number;
    buyPrice: number;
  } & SaleItemDetail_Key)[];
}
```
### Using `GetActiveSaleItems`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getActiveSaleItems, GetActiveSaleItemsVariables } from '@storebook/dataconnect';

// The `GetActiveSaleItems` query requires an argument of type `GetActiveSaleItemsVariables`:
const getActiveSaleItemsVars: GetActiveSaleItemsVariables = {
  storeId: ..., 
};

// Call the `getActiveSaleItems()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getActiveSaleItems(getActiveSaleItemsVars);
// Variables can be defined inline as well.
const { data } = await getActiveSaleItems({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getActiveSaleItems(dataConnect, getActiveSaleItemsVars);

console.log(data.saleItemDetails);

// Or, you can use the `Promise` API.
getActiveSaleItems(getActiveSaleItemsVars).then((response) => {
  const data = response.data;
  console.log(data.saleItemDetails);
});
```

### Using `GetActiveSaleItems`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getActiveSaleItemsRef, GetActiveSaleItemsVariables } from '@storebook/dataconnect';

// The `GetActiveSaleItems` query requires an argument of type `GetActiveSaleItemsVariables`:
const getActiveSaleItemsVars: GetActiveSaleItemsVariables = {
  storeId: ..., 
};

// Call the `getActiveSaleItemsRef()` function to get a reference to the query.
const ref = getActiveSaleItemsRef(getActiveSaleItemsVars);
// Variables can be defined inline as well.
const ref = getActiveSaleItemsRef({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getActiveSaleItemsRef(dataConnect, getActiveSaleItemsVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.saleItemDetails);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.saleItemDetails);
});
```

## GetActiveUdhaars
You can execute the `GetActiveUdhaars` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getActiveUdhaars(vars: GetActiveUdhaarsVariables): QueryPromise<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;

getActiveUdhaarsRef(vars: GetActiveUdhaarsVariables): QueryRef<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getActiveUdhaars(dc: DataConnect, vars: GetActiveUdhaarsVariables): QueryPromise<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;

getActiveUdhaarsRef(dc: DataConnect, vars: GetActiveUdhaarsVariables): QueryRef<GetActiveUdhaarsData, GetActiveUdhaarsVariables>;
```

### Variables
The `GetActiveUdhaars` query requires an argument of type `GetActiveUdhaarsVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetActiveUdhaarsVariables {
  storeId: string;
}
```
### Return Type
Recall that executing the `GetActiveUdhaars` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetActiveUdhaarsData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetActiveUdhaarsData {
  udhaarEntries: ({
    id: string;
    customerName: string;
    amount: number;
    type: string;
    timestamp: number;
    notes?: string | null;
    updatedAt: number;
  } & UdhaarEntry_Key)[];
}
```
### Using `GetActiveUdhaars`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getActiveUdhaars, GetActiveUdhaarsVariables } from '@storebook/dataconnect';

// The `GetActiveUdhaars` query requires an argument of type `GetActiveUdhaarsVariables`:
const getActiveUdhaarsVars: GetActiveUdhaarsVariables = {
  storeId: ..., 
};

// Call the `getActiveUdhaars()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getActiveUdhaars(getActiveUdhaarsVars);
// Variables can be defined inline as well.
const { data } = await getActiveUdhaars({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getActiveUdhaars(dataConnect, getActiveUdhaarsVars);

console.log(data.udhaarEntries);

// Or, you can use the `Promise` API.
getActiveUdhaars(getActiveUdhaarsVars).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntries);
});
```

### Using `GetActiveUdhaars`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getActiveUdhaarsRef, GetActiveUdhaarsVariables } from '@storebook/dataconnect';

// The `GetActiveUdhaars` query requires an argument of type `GetActiveUdhaarsVariables`:
const getActiveUdhaarsVars: GetActiveUdhaarsVariables = {
  storeId: ..., 
};

// Call the `getActiveUdhaarsRef()` function to get a reference to the query.
const ref = getActiveUdhaarsRef(getActiveUdhaarsVars);
// Variables can be defined inline as well.
const ref = getActiveUdhaarsRef({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getActiveUdhaarsRef(dataConnect, getActiveUdhaarsVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.udhaarEntries);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntries);
});
```

## GetActiveExpenses
You can execute the `GetActiveExpenses` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getActiveExpenses(vars: GetActiveExpensesVariables): QueryPromise<GetActiveExpensesData, GetActiveExpensesVariables>;

getActiveExpensesRef(vars: GetActiveExpensesVariables): QueryRef<GetActiveExpensesData, GetActiveExpensesVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getActiveExpenses(dc: DataConnect, vars: GetActiveExpensesVariables): QueryPromise<GetActiveExpensesData, GetActiveExpensesVariables>;

getActiveExpensesRef(dc: DataConnect, vars: GetActiveExpensesVariables): QueryRef<GetActiveExpensesData, GetActiveExpensesVariables>;
```

### Variables
The `GetActiveExpenses` query requires an argument of type `GetActiveExpensesVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetActiveExpensesVariables {
  storeId: string;
}
```
### Return Type
Recall that executing the `GetActiveExpenses` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetActiveExpensesData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetActiveExpensesData {
  expenseEntries: ({
    id: string;
    type: string;
    description: string;
    amount: number;
    timestamp: number;
    supplierName?: string | null;
    updatedAt: number;
  } & ExpenseEntry_Key)[];
}
```
### Using `GetActiveExpenses`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getActiveExpenses, GetActiveExpensesVariables } from '@storebook/dataconnect';

// The `GetActiveExpenses` query requires an argument of type `GetActiveExpensesVariables`:
const getActiveExpensesVars: GetActiveExpensesVariables = {
  storeId: ..., 
};

// Call the `getActiveExpenses()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getActiveExpenses(getActiveExpensesVars);
// Variables can be defined inline as well.
const { data } = await getActiveExpenses({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getActiveExpenses(dataConnect, getActiveExpensesVars);

console.log(data.expenseEntries);

// Or, you can use the `Promise` API.
getActiveExpenses(getActiveExpensesVars).then((response) => {
  const data = response.data;
  console.log(data.expenseEntries);
});
```

### Using `GetActiveExpenses`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getActiveExpensesRef, GetActiveExpensesVariables } from '@storebook/dataconnect';

// The `GetActiveExpenses` query requires an argument of type `GetActiveExpensesVariables`:
const getActiveExpensesVars: GetActiveExpensesVariables = {
  storeId: ..., 
};

// Call the `getActiveExpensesRef()` function to get a reference to the query.
const ref = getActiveExpensesRef(getActiveExpensesVars);
// Variables can be defined inline as well.
const ref = getActiveExpensesRef({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getActiveExpensesRef(dataConnect, getActiveExpensesVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.expenseEntries);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.expenseEntries);
});
```

## GetActiveSuppliers
You can execute the `GetActiveSuppliers` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getActiveSuppliers(vars: GetActiveSuppliersVariables): QueryPromise<GetActiveSuppliersData, GetActiveSuppliersVariables>;

getActiveSuppliersRef(vars: GetActiveSuppliersVariables): QueryRef<GetActiveSuppliersData, GetActiveSuppliersVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getActiveSuppliers(dc: DataConnect, vars: GetActiveSuppliersVariables): QueryPromise<GetActiveSuppliersData, GetActiveSuppliersVariables>;

getActiveSuppliersRef(dc: DataConnect, vars: GetActiveSuppliersVariables): QueryRef<GetActiveSuppliersData, GetActiveSuppliersVariables>;
```

### Variables
The `GetActiveSuppliers` query requires an argument of type `GetActiveSuppliersVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetActiveSuppliersVariables {
  storeId: string;
}
```
### Return Type
Recall that executing the `GetActiveSuppliers` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetActiveSuppliersData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetActiveSuppliersData {
  suppliers: ({
    id: string;
    name: string;
    phone?: string | null;
    gstin?: string | null;
    address?: string | null;
    updatedAt: number;
  } & Supplier_Key)[];
}
```
### Using `GetActiveSuppliers`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getActiveSuppliers, GetActiveSuppliersVariables } from '@storebook/dataconnect';

// The `GetActiveSuppliers` query requires an argument of type `GetActiveSuppliersVariables`:
const getActiveSuppliersVars: GetActiveSuppliersVariables = {
  storeId: ..., 
};

// Call the `getActiveSuppliers()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getActiveSuppliers(getActiveSuppliersVars);
// Variables can be defined inline as well.
const { data } = await getActiveSuppliers({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getActiveSuppliers(dataConnect, getActiveSuppliersVars);

console.log(data.suppliers);

// Or, you can use the `Promise` API.
getActiveSuppliers(getActiveSuppliersVars).then((response) => {
  const data = response.data;
  console.log(data.suppliers);
});
```

### Using `GetActiveSuppliers`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getActiveSuppliersRef, GetActiveSuppliersVariables } from '@storebook/dataconnect';

// The `GetActiveSuppliers` query requires an argument of type `GetActiveSuppliersVariables`:
const getActiveSuppliersVars: GetActiveSuppliersVariables = {
  storeId: ..., 
};

// Call the `getActiveSuppliersRef()` function to get a reference to the query.
const ref = getActiveSuppliersRef(getActiveSuppliersVars);
// Variables can be defined inline as well.
const ref = getActiveSuppliersRef({ storeId: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getActiveSuppliersRef(dataConnect, getActiveSuppliersVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.suppliers);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.suppliers);
});
```

## GetUser
You can execute the `GetUser` query using the following action shortcut function, or by calling `executeQuery()` after calling the following `QueryRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
getUser(vars: GetUserVariables): QueryPromise<GetUserData, GetUserVariables>;

getUserRef(vars: GetUserVariables): QueryRef<GetUserData, GetUserVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `QueryRef` function.
```javascript
getUser(dc: DataConnect, vars: GetUserVariables): QueryPromise<GetUserData, GetUserVariables>;

getUserRef(dc: DataConnect, vars: GetUserVariables): QueryRef<GetUserData, GetUserVariables>;
```

### Variables
The `GetUser` query requires an argument of type `GetUserVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface GetUserVariables {
  id: string;
}
```
### Return Type
Recall that executing the `GetUser` query returns a `QueryPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `GetUserData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface GetUserData {
  user?: {
    id: string;
    phoneNumber?: string | null;
    role: string;
    stores?: string[] | null;
    storeId?: string | null;
  } & User_Key;
}
```
### Using `GetUser`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, getUser, GetUserVariables } from '@storebook/dataconnect';

// The `GetUser` query requires an argument of type `GetUserVariables`:
const getUserVars: GetUserVariables = {
  id: ..., 
};

// Call the `getUser()` function to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await getUser(getUserVars);
// Variables can be defined inline as well.
const { data } = await getUser({ id: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await getUser(dataConnect, getUserVars);

console.log(data.user);

// Or, you can use the `Promise` API.
getUser(getUserVars).then((response) => {
  const data = response.data;
  console.log(data.user);
});
```

### Using `GetUser`'s `QueryRef` function

```javascript
import { getDataConnect, DataConnect, executeQuery } from 'firebase/data-connect';
import { connectorConfig, getUserRef, GetUserVariables } from '@storebook/dataconnect';

// The `GetUser` query requires an argument of type `GetUserVariables`:
const getUserVars: GetUserVariables = {
  id: ..., 
};

// Call the `getUserRef()` function to get a reference to the query.
const ref = getUserRef(getUserVars);
// Variables can be defined inline as well.
const ref = getUserRef({ id: ..., });

// You can also pass in a `DataConnect` instance to the `QueryRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = getUserRef(dataConnect, getUserVars);

// Call `executeQuery()` on the reference to execute the query.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeQuery(ref);

console.log(data.user);

// Or, you can use the `Promise` API.
executeQuery(ref).then((response) => {
  const data = response.data;
  console.log(data.user);
});
```

# Mutations

There are two ways to execute a Data Connect Mutation using the generated Web SDK:
- Using a Mutation Reference function, which returns a `MutationRef`
  - The `MutationRef` can be used as an argument to `executeMutation()`, which will execute the Mutation and return a `MutationPromise`
- Using an action shortcut function, which returns a `MutationPromise`
  - Calling the action shortcut function will execute the Mutation and return a `MutationPromise`

The following is true for both the action shortcut function and the `MutationRef` function:
- The `MutationPromise` returned will resolve to the result of the Mutation once it has finished executing
- If the Mutation accepts arguments, both the action shortcut function and the `MutationRef` function accept a single argument: an object that contains all the required variables (and the optional variables) for the Mutation
- Both functions can be called with or without passing in a `DataConnect` instance as an argument. If no `DataConnect` argument is passed in, then the generated SDK will call `getDataConnect(connectorConfig)` behind the scenes for you.

Below are examples of how to use the `storebook-connector` connector's generated functions to execute each mutation. You can also follow the examples from the [Data Connect documentation](https://firebase.google.com/docs/data-connect/web-sdk#using-mutations).

## SyncItem
You can execute the `SyncItem` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncItem(vars: SyncItemVariables): MutationPromise<SyncItemData, SyncItemVariables>;

syncItemRef(vars: SyncItemVariables): MutationRef<SyncItemData, SyncItemVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncItem(dc: DataConnect, vars: SyncItemVariables): MutationPromise<SyncItemData, SyncItemVariables>;

syncItemRef(dc: DataConnect, vars: SyncItemVariables): MutationRef<SyncItemData, SyncItemVariables>;
```

### Variables
The `SyncItem` mutation requires an argument of type `SyncItemVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncItemVariables {
  id: string;
  storeId: string;
  name: string;
  quantity: number;
  unit: string;
  buyPrice: number;
  sellPrice: number;
  lowStockThreshold: number;
  category: string;
  photoPath?: string | null;
  hsnCode?: string | null;
  taxRate?: number | null;
  batchLotNumber?: string | null;
  expiryDate?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SyncItem` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncItemData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncItemData {
  item_upsert: Item_Key;
}
```
### Using `SyncItem`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncItem, SyncItemVariables } from '@storebook/dataconnect';

// The `SyncItem` mutation requires an argument of type `SyncItemVariables`:
const syncItemVars: SyncItemVariables = {
  id: ..., 
  storeId: ..., 
  name: ..., 
  quantity: ..., 
  unit: ..., 
  buyPrice: ..., 
  sellPrice: ..., 
  lowStockThreshold: ..., 
  category: ..., 
  photoPath: ..., // optional
  hsnCode: ..., // optional
  taxRate: ..., // optional
  batchLotNumber: ..., // optional
  expiryDate: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncItem()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncItem(syncItemVars);
// Variables can be defined inline as well.
const { data } = await syncItem({ id: ..., storeId: ..., name: ..., quantity: ..., unit: ..., buyPrice: ..., sellPrice: ..., lowStockThreshold: ..., category: ..., photoPath: ..., hsnCode: ..., taxRate: ..., batchLotNumber: ..., expiryDate: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncItem(dataConnect, syncItemVars);

console.log(data.item_upsert);

// Or, you can use the `Promise` API.
syncItem(syncItemVars).then((response) => {
  const data = response.data;
  console.log(data.item_upsert);
});
```

### Using `SyncItem`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncItemRef, SyncItemVariables } from '@storebook/dataconnect';

// The `SyncItem` mutation requires an argument of type `SyncItemVariables`:
const syncItemVars: SyncItemVariables = {
  id: ..., 
  storeId: ..., 
  name: ..., 
  quantity: ..., 
  unit: ..., 
  buyPrice: ..., 
  sellPrice: ..., 
  lowStockThreshold: ..., 
  category: ..., 
  photoPath: ..., // optional
  hsnCode: ..., // optional
  taxRate: ..., // optional
  batchLotNumber: ..., // optional
  expiryDate: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncItemRef()` function to get a reference to the mutation.
const ref = syncItemRef(syncItemVars);
// Variables can be defined inline as well.
const ref = syncItemRef({ id: ..., storeId: ..., name: ..., quantity: ..., unit: ..., buyPrice: ..., sellPrice: ..., lowStockThreshold: ..., category: ..., photoPath: ..., hsnCode: ..., taxRate: ..., batchLotNumber: ..., expiryDate: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncItemRef(dataConnect, syncItemVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.item_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.item_upsert);
});
```

## SyncSale
You can execute the `SyncSale` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncSale(vars: SyncSaleVariables): MutationPromise<SyncSaleData, SyncSaleVariables>;

syncSaleRef(vars: SyncSaleVariables): MutationRef<SyncSaleData, SyncSaleVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncSale(dc: DataConnect, vars: SyncSaleVariables): MutationPromise<SyncSaleData, SyncSaleVariables>;

syncSaleRef(dc: DataConnect, vars: SyncSaleVariables): MutationRef<SyncSaleData, SyncSaleVariables>;
```

### Variables
The `SyncSale` mutation requires an argument of type `SyncSaleVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncSaleVariables {
  id: string;
  storeId: string;
  timestamp: number;
  totalAmount: number;
  discountAmount: number;
  customerName?: string | null;
  customerGstin?: string | null;
  businessGstin?: string | null;
  customerAddress?: string | null;
  businessAddress?: string | null;
  type: string;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SyncSale` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncSaleData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncSaleData {
  sale_upsert: Sale_Key;
}
```
### Using `SyncSale`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncSale, SyncSaleVariables } from '@storebook/dataconnect';

// The `SyncSale` mutation requires an argument of type `SyncSaleVariables`:
const syncSaleVars: SyncSaleVariables = {
  id: ..., 
  storeId: ..., 
  timestamp: ..., 
  totalAmount: ..., 
  discountAmount: ..., 
  customerName: ..., // optional
  customerGstin: ..., // optional
  businessGstin: ..., // optional
  customerAddress: ..., // optional
  businessAddress: ..., // optional
  type: ..., 
  notes: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncSale()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncSale(syncSaleVars);
// Variables can be defined inline as well.
const { data } = await syncSale({ id: ..., storeId: ..., timestamp: ..., totalAmount: ..., discountAmount: ..., customerName: ..., customerGstin: ..., businessGstin: ..., customerAddress: ..., businessAddress: ..., type: ..., notes: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncSale(dataConnect, syncSaleVars);

console.log(data.sale_upsert);

// Or, you can use the `Promise` API.
syncSale(syncSaleVars).then((response) => {
  const data = response.data;
  console.log(data.sale_upsert);
});
```

### Using `SyncSale`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncSaleRef, SyncSaleVariables } from '@storebook/dataconnect';

// The `SyncSale` mutation requires an argument of type `SyncSaleVariables`:
const syncSaleVars: SyncSaleVariables = {
  id: ..., 
  storeId: ..., 
  timestamp: ..., 
  totalAmount: ..., 
  discountAmount: ..., 
  customerName: ..., // optional
  customerGstin: ..., // optional
  businessGstin: ..., // optional
  customerAddress: ..., // optional
  businessAddress: ..., // optional
  type: ..., 
  notes: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncSaleRef()` function to get a reference to the mutation.
const ref = syncSaleRef(syncSaleVars);
// Variables can be defined inline as well.
const ref = syncSaleRef({ id: ..., storeId: ..., timestamp: ..., totalAmount: ..., discountAmount: ..., customerName: ..., customerGstin: ..., businessGstin: ..., customerAddress: ..., businessAddress: ..., type: ..., notes: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncSaleRef(dataConnect, syncSaleVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.sale_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.sale_upsert);
});
```

## SyncSaleItem
You can execute the `SyncSaleItem` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncSaleItem(vars: SyncSaleItemVariables): MutationPromise<SyncSaleItemData, SyncSaleItemVariables>;

syncSaleItemRef(vars: SyncSaleItemVariables): MutationRef<SyncSaleItemData, SyncSaleItemVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncSaleItem(dc: DataConnect, vars: SyncSaleItemVariables): MutationPromise<SyncSaleItemData, SyncSaleItemVariables>;

syncSaleItemRef(dc: DataConnect, vars: SyncSaleItemVariables): MutationRef<SyncSaleItemData, SyncSaleItemVariables>;
```

### Variables
The `SyncSaleItem` mutation requires an argument of type `SyncSaleItemVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncSaleItemVariables {
  id: string;
  storeId: string;
  saleId: string;
  itemId: string;
  itemName: string;
  unit: string;
  quantity: number;
  sellPrice: number;
  buyPrice: number;
  isDeleted: boolean;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SyncSaleItem` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncSaleItemData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncSaleItemData {
  saleItemDetail_upsert: SaleItemDetail_Key;
}
```
### Using `SyncSaleItem`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncSaleItem, SyncSaleItemVariables } from '@storebook/dataconnect';

// The `SyncSaleItem` mutation requires an argument of type `SyncSaleItemVariables`:
const syncSaleItemVars: SyncSaleItemVariables = {
  id: ..., 
  storeId: ..., 
  saleId: ..., 
  itemId: ..., 
  itemName: ..., 
  unit: ..., 
  quantity: ..., 
  sellPrice: ..., 
  buyPrice: ..., 
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncSaleItem()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncSaleItem(syncSaleItemVars);
// Variables can be defined inline as well.
const { data } = await syncSaleItem({ id: ..., storeId: ..., saleId: ..., itemId: ..., itemName: ..., unit: ..., quantity: ..., sellPrice: ..., buyPrice: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncSaleItem(dataConnect, syncSaleItemVars);

console.log(data.saleItemDetail_upsert);

// Or, you can use the `Promise` API.
syncSaleItem(syncSaleItemVars).then((response) => {
  const data = response.data;
  console.log(data.saleItemDetail_upsert);
});
```

### Using `SyncSaleItem`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncSaleItemRef, SyncSaleItemVariables } from '@storebook/dataconnect';

// The `SyncSaleItem` mutation requires an argument of type `SyncSaleItemVariables`:
const syncSaleItemVars: SyncSaleItemVariables = {
  id: ..., 
  storeId: ..., 
  saleId: ..., 
  itemId: ..., 
  itemName: ..., 
  unit: ..., 
  quantity: ..., 
  sellPrice: ..., 
  buyPrice: ..., 
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncSaleItemRef()` function to get a reference to the mutation.
const ref = syncSaleItemRef(syncSaleItemVars);
// Variables can be defined inline as well.
const ref = syncSaleItemRef({ id: ..., storeId: ..., saleId: ..., itemId: ..., itemName: ..., unit: ..., quantity: ..., sellPrice: ..., buyPrice: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncSaleItemRef(dataConnect, syncSaleItemVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.saleItemDetail_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.saleItemDetail_upsert);
});
```

## SoftDeleteItem
You can execute the `SoftDeleteItem` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
softDeleteItem(vars: SoftDeleteItemVariables): MutationPromise<SoftDeleteItemData, SoftDeleteItemVariables>;

softDeleteItemRef(vars: SoftDeleteItemVariables): MutationRef<SoftDeleteItemData, SoftDeleteItemVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
softDeleteItem(dc: DataConnect, vars: SoftDeleteItemVariables): MutationPromise<SoftDeleteItemData, SoftDeleteItemVariables>;

softDeleteItemRef(dc: DataConnect, vars: SoftDeleteItemVariables): MutationRef<SoftDeleteItemData, SoftDeleteItemVariables>;
```

### Variables
The `SoftDeleteItem` mutation requires an argument of type `SoftDeleteItemVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SoftDeleteItemVariables {
  id: string;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SoftDeleteItem` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SoftDeleteItemData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SoftDeleteItemData {
  item_update?: Item_Key | null;
}
```
### Using `SoftDeleteItem`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, softDeleteItem, SoftDeleteItemVariables } from '@storebook/dataconnect';

// The `SoftDeleteItem` mutation requires an argument of type `SoftDeleteItemVariables`:
const softDeleteItemVars: SoftDeleteItemVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteItem()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await softDeleteItem(softDeleteItemVars);
// Variables can be defined inline as well.
const { data } = await softDeleteItem({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await softDeleteItem(dataConnect, softDeleteItemVars);

console.log(data.item_update);

// Or, you can use the `Promise` API.
softDeleteItem(softDeleteItemVars).then((response) => {
  const data = response.data;
  console.log(data.item_update);
});
```

### Using `SoftDeleteItem`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, softDeleteItemRef, SoftDeleteItemVariables } from '@storebook/dataconnect';

// The `SoftDeleteItem` mutation requires an argument of type `SoftDeleteItemVariables`:
const softDeleteItemVars: SoftDeleteItemVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteItemRef()` function to get a reference to the mutation.
const ref = softDeleteItemRef(softDeleteItemVars);
// Variables can be defined inline as well.
const ref = softDeleteItemRef({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = softDeleteItemRef(dataConnect, softDeleteItemVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.item_update);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.item_update);
});
```

## SoftDeleteSale
You can execute the `SoftDeleteSale` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
softDeleteSale(vars: SoftDeleteSaleVariables): MutationPromise<SoftDeleteSaleData, SoftDeleteSaleVariables>;

softDeleteSaleRef(vars: SoftDeleteSaleVariables): MutationRef<SoftDeleteSaleData, SoftDeleteSaleVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
softDeleteSale(dc: DataConnect, vars: SoftDeleteSaleVariables): MutationPromise<SoftDeleteSaleData, SoftDeleteSaleVariables>;

softDeleteSaleRef(dc: DataConnect, vars: SoftDeleteSaleVariables): MutationRef<SoftDeleteSaleData, SoftDeleteSaleVariables>;
```

### Variables
The `SoftDeleteSale` mutation requires an argument of type `SoftDeleteSaleVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SoftDeleteSaleVariables {
  id: string;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SoftDeleteSale` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SoftDeleteSaleData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SoftDeleteSaleData {
  sale_update?: Sale_Key | null;
}
```
### Using `SoftDeleteSale`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, softDeleteSale, SoftDeleteSaleVariables } from '@storebook/dataconnect';

// The `SoftDeleteSale` mutation requires an argument of type `SoftDeleteSaleVariables`:
const softDeleteSaleVars: SoftDeleteSaleVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteSale()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await softDeleteSale(softDeleteSaleVars);
// Variables can be defined inline as well.
const { data } = await softDeleteSale({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await softDeleteSale(dataConnect, softDeleteSaleVars);

console.log(data.sale_update);

// Or, you can use the `Promise` API.
softDeleteSale(softDeleteSaleVars).then((response) => {
  const data = response.data;
  console.log(data.sale_update);
});
```

### Using `SoftDeleteSale`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, softDeleteSaleRef, SoftDeleteSaleVariables } from '@storebook/dataconnect';

// The `SoftDeleteSale` mutation requires an argument of type `SoftDeleteSaleVariables`:
const softDeleteSaleVars: SoftDeleteSaleVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteSaleRef()` function to get a reference to the mutation.
const ref = softDeleteSaleRef(softDeleteSaleVars);
// Variables can be defined inline as well.
const ref = softDeleteSaleRef({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = softDeleteSaleRef(dataConnect, softDeleteSaleVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.sale_update);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.sale_update);
});
```

## SyncUser
You can execute the `SyncUser` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncUser(vars: SyncUserVariables): MutationPromise<SyncUserData, SyncUserVariables>;

syncUserRef(vars: SyncUserVariables): MutationRef<SyncUserData, SyncUserVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncUser(dc: DataConnect, vars: SyncUserVariables): MutationPromise<SyncUserData, SyncUserVariables>;

syncUserRef(dc: DataConnect, vars: SyncUserVariables): MutationRef<SyncUserData, SyncUserVariables>;
```

### Variables
The `SyncUser` mutation requires an argument of type `SyncUserVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncUserVariables {
  id: string;
  phoneNumber?: string | null;
  username?: string | null;
  createdAt: number;
  role: string;
  stores?: string[] | null;
  storeId?: string | null;
  ownerId?: string | null;
  subscriptionStatus?: string | null;
  subscriptionPlan?: string | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
}
```
### Return Type
Recall that executing the `SyncUser` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncUserData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncUserData {
  user_upsert: User_Key;
}
```
### Using `SyncUser`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncUser, SyncUserVariables } from '@storebook/dataconnect';

// The `SyncUser` mutation requires an argument of type `SyncUserVariables`:
const syncUserVars: SyncUserVariables = {
  id: ..., 
  phoneNumber: ..., // optional
  username: ..., // optional
  createdAt: ..., 
  role: ..., 
  stores: ..., // optional
  storeId: ..., // optional
  ownerId: ..., // optional
  subscriptionStatus: ..., // optional
  subscriptionPlan: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
};

// Call the `syncUser()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncUser(syncUserVars);
// Variables can be defined inline as well.
const { data } = await syncUser({ id: ..., phoneNumber: ..., username: ..., createdAt: ..., role: ..., stores: ..., storeId: ..., ownerId: ..., subscriptionStatus: ..., subscriptionPlan: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncUser(dataConnect, syncUserVars);

console.log(data.user_upsert);

// Or, you can use the `Promise` API.
syncUser(syncUserVars).then((response) => {
  const data = response.data;
  console.log(data.user_upsert);
});
```

### Using `SyncUser`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncUserRef, SyncUserVariables } from '@storebook/dataconnect';

// The `SyncUser` mutation requires an argument of type `SyncUserVariables`:
const syncUserVars: SyncUserVariables = {
  id: ..., 
  phoneNumber: ..., // optional
  username: ..., // optional
  createdAt: ..., 
  role: ..., 
  stores: ..., // optional
  storeId: ..., // optional
  ownerId: ..., // optional
  subscriptionStatus: ..., // optional
  subscriptionPlan: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
};

// Call the `syncUserRef()` function to get a reference to the mutation.
const ref = syncUserRef(syncUserVars);
// Variables can be defined inline as well.
const ref = syncUserRef({ id: ..., phoneNumber: ..., username: ..., createdAt: ..., role: ..., stores: ..., storeId: ..., ownerId: ..., subscriptionStatus: ..., subscriptionPlan: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncUserRef(dataConnect, syncUserVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.user_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.user_upsert);
});
```

## UpdateUser
You can execute the `UpdateUser` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
updateUser(vars: UpdateUserVariables): MutationPromise<UpdateUserData, UpdateUserVariables>;

updateUserRef(vars: UpdateUserVariables): MutationRef<UpdateUserData, UpdateUserVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
updateUser(dc: DataConnect, vars: UpdateUserVariables): MutationPromise<UpdateUserData, UpdateUserVariables>;

updateUserRef(dc: DataConnect, vars: UpdateUserVariables): MutationRef<UpdateUserData, UpdateUserVariables>;
```

### Variables
The `UpdateUser` mutation requires an argument of type `UpdateUserVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface UpdateUserVariables {
  id: string;
  subscriptionStatus?: string | null;
  subscriptionPlan?: string | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
}
```
### Return Type
Recall that executing the `UpdateUser` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `UpdateUserData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface UpdateUserData {
  user_update?: User_Key | null;
}
```
### Using `UpdateUser`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, updateUser, UpdateUserVariables } from '@storebook/dataconnect';

// The `UpdateUser` mutation requires an argument of type `UpdateUserVariables`:
const updateUserVars: UpdateUserVariables = {
  id: ..., 
  subscriptionStatus: ..., // optional
  subscriptionPlan: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
};

// Call the `updateUser()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await updateUser(updateUserVars);
// Variables can be defined inline as well.
const { data } = await updateUser({ id: ..., subscriptionStatus: ..., subscriptionPlan: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await updateUser(dataConnect, updateUserVars);

console.log(data.user_update);

// Or, you can use the `Promise` API.
updateUser(updateUserVars).then((response) => {
  const data = response.data;
  console.log(data.user_update);
});
```

### Using `UpdateUser`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, updateUserRef, UpdateUserVariables } from '@storebook/dataconnect';

// The `UpdateUser` mutation requires an argument of type `UpdateUserVariables`:
const updateUserVars: UpdateUserVariables = {
  id: ..., 
  subscriptionStatus: ..., // optional
  subscriptionPlan: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
};

// Call the `updateUserRef()` function to get a reference to the mutation.
const ref = updateUserRef(updateUserVars);
// Variables can be defined inline as well.
const ref = updateUserRef({ id: ..., subscriptionStatus: ..., subscriptionPlan: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = updateUserRef(dataConnect, updateUserVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.user_update);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.user_update);
});
```

## SyncStore
You can execute the `SyncStore` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncStore(vars: SyncStoreVariables): MutationPromise<SyncStoreData, SyncStoreVariables>;

syncStoreRef(vars: SyncStoreVariables): MutationRef<SyncStoreData, SyncStoreVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncStore(dc: DataConnect, vars: SyncStoreVariables): MutationPromise<SyncStoreData, SyncStoreVariables>;

syncStoreRef(dc: DataConnect, vars: SyncStoreVariables): MutationRef<SyncStoreData, SyncStoreVariables>;
```

### Variables
The `SyncStore` mutation requires an argument of type `SyncStoreVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncStoreVariables {
  id: string;
  name?: string | null;
  isActive?: boolean | null;
  isPremium?: boolean | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
  subscriptionStatus?: string | null;
}
```
### Return Type
Recall that executing the `SyncStore` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncStoreData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncStoreData {
  store_upsert: Store_Key;
}
```
### Using `SyncStore`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncStore, SyncStoreVariables } from '@storebook/dataconnect';

// The `SyncStore` mutation requires an argument of type `SyncStoreVariables`:
const syncStoreVars: SyncStoreVariables = {
  id: ..., 
  name: ..., // optional
  isActive: ..., // optional
  isPremium: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
  subscriptionStatus: ..., // optional
};

// Call the `syncStore()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncStore(syncStoreVars);
// Variables can be defined inline as well.
const { data } = await syncStore({ id: ..., name: ..., isActive: ..., isPremium: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., subscriptionStatus: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncStore(dataConnect, syncStoreVars);

console.log(data.store_upsert);

// Or, you can use the `Promise` API.
syncStore(syncStoreVars).then((response) => {
  const data = response.data;
  console.log(data.store_upsert);
});
```

### Using `SyncStore`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncStoreRef, SyncStoreVariables } from '@storebook/dataconnect';

// The `SyncStore` mutation requires an argument of type `SyncStoreVariables`:
const syncStoreVars: SyncStoreVariables = {
  id: ..., 
  name: ..., // optional
  isActive: ..., // optional
  isPremium: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
  subscriptionStatus: ..., // optional
};

// Call the `syncStoreRef()` function to get a reference to the mutation.
const ref = syncStoreRef(syncStoreVars);
// Variables can be defined inline as well.
const ref = syncStoreRef({ id: ..., name: ..., isActive: ..., isPremium: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., subscriptionStatus: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncStoreRef(dataConnect, syncStoreVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.store_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.store_upsert);
});
```

## UpdateStore
You can execute the `UpdateStore` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
updateStore(vars: UpdateStoreVariables): MutationPromise<UpdateStoreData, UpdateStoreVariables>;

updateStoreRef(vars: UpdateStoreVariables): MutationRef<UpdateStoreData, UpdateStoreVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
updateStore(dc: DataConnect, vars: UpdateStoreVariables): MutationPromise<UpdateStoreData, UpdateStoreVariables>;

updateStoreRef(dc: DataConnect, vars: UpdateStoreVariables): MutationRef<UpdateStoreData, UpdateStoreVariables>;
```

### Variables
The `UpdateStore` mutation requires an argument of type `UpdateStoreVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface UpdateStoreVariables {
  id: string;
  isPremium?: boolean | null;
  subscriptionExpiresAt?: number | null;
  subscriptionPlatform?: string | null;
  subscriptionId?: string | null;
  subscriptionStatus?: string | null;
}
```
### Return Type
Recall that executing the `UpdateStore` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `UpdateStoreData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface UpdateStoreData {
  store_update?: Store_Key | null;
}
```
### Using `UpdateStore`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, updateStore, UpdateStoreVariables } from '@storebook/dataconnect';

// The `UpdateStore` mutation requires an argument of type `UpdateStoreVariables`:
const updateStoreVars: UpdateStoreVariables = {
  id: ..., 
  isPremium: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
  subscriptionStatus: ..., // optional
};

// Call the `updateStore()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await updateStore(updateStoreVars);
// Variables can be defined inline as well.
const { data } = await updateStore({ id: ..., isPremium: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., subscriptionStatus: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await updateStore(dataConnect, updateStoreVars);

console.log(data.store_update);

// Or, you can use the `Promise` API.
updateStore(updateStoreVars).then((response) => {
  const data = response.data;
  console.log(data.store_update);
});
```

### Using `UpdateStore`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, updateStoreRef, UpdateStoreVariables } from '@storebook/dataconnect';

// The `UpdateStore` mutation requires an argument of type `UpdateStoreVariables`:
const updateStoreVars: UpdateStoreVariables = {
  id: ..., 
  isPremium: ..., // optional
  subscriptionExpiresAt: ..., // optional
  subscriptionPlatform: ..., // optional
  subscriptionId: ..., // optional
  subscriptionStatus: ..., // optional
};

// Call the `updateStoreRef()` function to get a reference to the mutation.
const ref = updateStoreRef(updateStoreVars);
// Variables can be defined inline as well.
const ref = updateStoreRef({ id: ..., isPremium: ..., subscriptionExpiresAt: ..., subscriptionPlatform: ..., subscriptionId: ..., subscriptionStatus: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = updateStoreRef(dataConnect, updateStoreVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.store_update);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.store_update);
});
```

## SyncUdhaar
You can execute the `SyncUdhaar` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncUdhaar(vars: SyncUdhaarVariables): MutationPromise<SyncUdhaarData, SyncUdhaarVariables>;

syncUdhaarRef(vars: SyncUdhaarVariables): MutationRef<SyncUdhaarData, SyncUdhaarVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncUdhaar(dc: DataConnect, vars: SyncUdhaarVariables): MutationPromise<SyncUdhaarData, SyncUdhaarVariables>;

syncUdhaarRef(dc: DataConnect, vars: SyncUdhaarVariables): MutationRef<SyncUdhaarData, SyncUdhaarVariables>;
```

### Variables
The `SyncUdhaar` mutation requires an argument of type `SyncUdhaarVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncUdhaarVariables {
  id: string;
  storeId: string;
  customerName: string;
  amount: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SyncUdhaar` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncUdhaarData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncUdhaarData {
  udhaarEntry_upsert: UdhaarEntry_Key;
}
```
### Using `SyncUdhaar`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncUdhaar, SyncUdhaarVariables } from '@storebook/dataconnect';

// The `SyncUdhaar` mutation requires an argument of type `SyncUdhaarVariables`:
const syncUdhaarVars: SyncUdhaarVariables = {
  id: ..., 
  storeId: ..., 
  customerName: ..., 
  amount: ..., 
  type: ..., 
  timestamp: ..., 
  notes: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncUdhaar()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncUdhaar(syncUdhaarVars);
// Variables can be defined inline as well.
const { data } = await syncUdhaar({ id: ..., storeId: ..., customerName: ..., amount: ..., type: ..., timestamp: ..., notes: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncUdhaar(dataConnect, syncUdhaarVars);

console.log(data.udhaarEntry_upsert);

// Or, you can use the `Promise` API.
syncUdhaar(syncUdhaarVars).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntry_upsert);
});
```

### Using `SyncUdhaar`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncUdhaarRef, SyncUdhaarVariables } from '@storebook/dataconnect';

// The `SyncUdhaar` mutation requires an argument of type `SyncUdhaarVariables`:
const syncUdhaarVars: SyncUdhaarVariables = {
  id: ..., 
  storeId: ..., 
  customerName: ..., 
  amount: ..., 
  type: ..., 
  timestamp: ..., 
  notes: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncUdhaarRef()` function to get a reference to the mutation.
const ref = syncUdhaarRef(syncUdhaarVars);
// Variables can be defined inline as well.
const ref = syncUdhaarRef({ id: ..., storeId: ..., customerName: ..., amount: ..., type: ..., timestamp: ..., notes: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncUdhaarRef(dataConnect, syncUdhaarVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.udhaarEntry_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntry_upsert);
});
```

## SoftDeleteUdhaar
You can execute the `SoftDeleteUdhaar` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
softDeleteUdhaar(vars: SoftDeleteUdhaarVariables): MutationPromise<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;

softDeleteUdhaarRef(vars: SoftDeleteUdhaarVariables): MutationRef<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
softDeleteUdhaar(dc: DataConnect, vars: SoftDeleteUdhaarVariables): MutationPromise<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;

softDeleteUdhaarRef(dc: DataConnect, vars: SoftDeleteUdhaarVariables): MutationRef<SoftDeleteUdhaarData, SoftDeleteUdhaarVariables>;
```

### Variables
The `SoftDeleteUdhaar` mutation requires an argument of type `SoftDeleteUdhaarVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SoftDeleteUdhaarVariables {
  id: string;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SoftDeleteUdhaar` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SoftDeleteUdhaarData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SoftDeleteUdhaarData {
  udhaarEntry_update?: UdhaarEntry_Key | null;
}
```
### Using `SoftDeleteUdhaar`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, softDeleteUdhaar, SoftDeleteUdhaarVariables } from '@storebook/dataconnect';

// The `SoftDeleteUdhaar` mutation requires an argument of type `SoftDeleteUdhaarVariables`:
const softDeleteUdhaarVars: SoftDeleteUdhaarVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteUdhaar()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await softDeleteUdhaar(softDeleteUdhaarVars);
// Variables can be defined inline as well.
const { data } = await softDeleteUdhaar({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await softDeleteUdhaar(dataConnect, softDeleteUdhaarVars);

console.log(data.udhaarEntry_update);

// Or, you can use the `Promise` API.
softDeleteUdhaar(softDeleteUdhaarVars).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntry_update);
});
```

### Using `SoftDeleteUdhaar`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, softDeleteUdhaarRef, SoftDeleteUdhaarVariables } from '@storebook/dataconnect';

// The `SoftDeleteUdhaar` mutation requires an argument of type `SoftDeleteUdhaarVariables`:
const softDeleteUdhaarVars: SoftDeleteUdhaarVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteUdhaarRef()` function to get a reference to the mutation.
const ref = softDeleteUdhaarRef(softDeleteUdhaarVars);
// Variables can be defined inline as well.
const ref = softDeleteUdhaarRef({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = softDeleteUdhaarRef(dataConnect, softDeleteUdhaarVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.udhaarEntry_update);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.udhaarEntry_update);
});
```

## SyncExpense
You can execute the `SyncExpense` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncExpense(vars: SyncExpenseVariables): MutationPromise<SyncExpenseData, SyncExpenseVariables>;

syncExpenseRef(vars: SyncExpenseVariables): MutationRef<SyncExpenseData, SyncExpenseVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncExpense(dc: DataConnect, vars: SyncExpenseVariables): MutationPromise<SyncExpenseData, SyncExpenseVariables>;

syncExpenseRef(dc: DataConnect, vars: SyncExpenseVariables): MutationRef<SyncExpenseData, SyncExpenseVariables>;
```

### Variables
The `SyncExpense` mutation requires an argument of type `SyncExpenseVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncExpenseVariables {
  id: string;
  storeId: string;
  type: string;
  description: string;
  amount: number;
  timestamp: number;
  supplierName?: string | null;
  supplierPhone?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SyncExpense` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncExpenseData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncExpenseData {
  expenseEntry_upsert: ExpenseEntry_Key;
}
```
### Using `SyncExpense`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncExpense, SyncExpenseVariables } from '@storebook/dataconnect';

// The `SyncExpense` mutation requires an argument of type `SyncExpenseVariables`:
const syncExpenseVars: SyncExpenseVariables = {
  id: ..., 
  storeId: ..., 
  type: ..., 
  description: ..., 
  amount: ..., 
  timestamp: ..., 
  supplierName: ..., // optional
  supplierPhone: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncExpense()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncExpense(syncExpenseVars);
// Variables can be defined inline as well.
const { data } = await syncExpense({ id: ..., storeId: ..., type: ..., description: ..., amount: ..., timestamp: ..., supplierName: ..., supplierPhone: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncExpense(dataConnect, syncExpenseVars);

console.log(data.expenseEntry_upsert);

// Or, you can use the `Promise` API.
syncExpense(syncExpenseVars).then((response) => {
  const data = response.data;
  console.log(data.expenseEntry_upsert);
});
```

### Using `SyncExpense`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncExpenseRef, SyncExpenseVariables } from '@storebook/dataconnect';

// The `SyncExpense` mutation requires an argument of type `SyncExpenseVariables`:
const syncExpenseVars: SyncExpenseVariables = {
  id: ..., 
  storeId: ..., 
  type: ..., 
  description: ..., 
  amount: ..., 
  timestamp: ..., 
  supplierName: ..., // optional
  supplierPhone: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncExpenseRef()` function to get a reference to the mutation.
const ref = syncExpenseRef(syncExpenseVars);
// Variables can be defined inline as well.
const ref = syncExpenseRef({ id: ..., storeId: ..., type: ..., description: ..., amount: ..., timestamp: ..., supplierName: ..., supplierPhone: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncExpenseRef(dataConnect, syncExpenseVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.expenseEntry_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.expenseEntry_upsert);
});
```

## SoftDeleteExpense
You can execute the `SoftDeleteExpense` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
softDeleteExpense(vars: SoftDeleteExpenseVariables): MutationPromise<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;

softDeleteExpenseRef(vars: SoftDeleteExpenseVariables): MutationRef<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
softDeleteExpense(dc: DataConnect, vars: SoftDeleteExpenseVariables): MutationPromise<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;

softDeleteExpenseRef(dc: DataConnect, vars: SoftDeleteExpenseVariables): MutationRef<SoftDeleteExpenseData, SoftDeleteExpenseVariables>;
```

### Variables
The `SoftDeleteExpense` mutation requires an argument of type `SoftDeleteExpenseVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SoftDeleteExpenseVariables {
  id: string;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SoftDeleteExpense` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SoftDeleteExpenseData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SoftDeleteExpenseData {
  expenseEntry_update?: ExpenseEntry_Key | null;
}
```
### Using `SoftDeleteExpense`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, softDeleteExpense, SoftDeleteExpenseVariables } from '@storebook/dataconnect';

// The `SoftDeleteExpense` mutation requires an argument of type `SoftDeleteExpenseVariables`:
const softDeleteExpenseVars: SoftDeleteExpenseVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteExpense()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await softDeleteExpense(softDeleteExpenseVars);
// Variables can be defined inline as well.
const { data } = await softDeleteExpense({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await softDeleteExpense(dataConnect, softDeleteExpenseVars);

console.log(data.expenseEntry_update);

// Or, you can use the `Promise` API.
softDeleteExpense(softDeleteExpenseVars).then((response) => {
  const data = response.data;
  console.log(data.expenseEntry_update);
});
```

### Using `SoftDeleteExpense`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, softDeleteExpenseRef, SoftDeleteExpenseVariables } from '@storebook/dataconnect';

// The `SoftDeleteExpense` mutation requires an argument of type `SoftDeleteExpenseVariables`:
const softDeleteExpenseVars: SoftDeleteExpenseVariables = {
  id: ..., 
  updatedAt: ..., 
};

// Call the `softDeleteExpenseRef()` function to get a reference to the mutation.
const ref = softDeleteExpenseRef(softDeleteExpenseVars);
// Variables can be defined inline as well.
const ref = softDeleteExpenseRef({ id: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = softDeleteExpenseRef(dataConnect, softDeleteExpenseVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.expenseEntry_update);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.expenseEntry_update);
});
```

## SyncSupplier
You can execute the `SyncSupplier` mutation using the following action shortcut function, or by calling `executeMutation()` after calling the following `MutationRef` function, both of which are defined in [dataconnect/index.d.ts](./index.d.ts):
```javascript
syncSupplier(vars: SyncSupplierVariables): MutationPromise<SyncSupplierData, SyncSupplierVariables>;

syncSupplierRef(vars: SyncSupplierVariables): MutationRef<SyncSupplierData, SyncSupplierVariables>;
```
You can also pass in a `DataConnect` instance to the action shortcut function or `MutationRef` function.
```javascript
syncSupplier(dc: DataConnect, vars: SyncSupplierVariables): MutationPromise<SyncSupplierData, SyncSupplierVariables>;

syncSupplierRef(dc: DataConnect, vars: SyncSupplierVariables): MutationRef<SyncSupplierData, SyncSupplierVariables>;
```

### Variables
The `SyncSupplier` mutation requires an argument of type `SyncSupplierVariables`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:

```javascript
export interface SyncSupplierVariables {
  id: string;
  storeId: string;
  name: string;
  phone?: string | null;
  gstin?: string | null;
  address?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}
```
### Return Type
Recall that executing the `SyncSupplier` mutation returns a `MutationPromise` that resolves to an object with a `data` property.

The `data` property is an object of type `SyncSupplierData`, which is defined in [dataconnect/index.d.ts](./index.d.ts). It has the following fields:
```javascript
export interface SyncSupplierData {
  supplier_upsert: Supplier_Key;
}
```
### Using `SyncSupplier`'s action shortcut function

```javascript
import { getDataConnect, DataConnect } from 'firebase/data-connect';
import { connectorConfig, syncSupplier, SyncSupplierVariables } from '@storebook/dataconnect';

// The `SyncSupplier` mutation requires an argument of type `SyncSupplierVariables`:
const syncSupplierVars: SyncSupplierVariables = {
  id: ..., 
  storeId: ..., 
  name: ..., 
  phone: ..., // optional
  gstin: ..., // optional
  address: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncSupplier()` function to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await syncSupplier(syncSupplierVars);
// Variables can be defined inline as well.
const { data } = await syncSupplier({ id: ..., storeId: ..., name: ..., phone: ..., gstin: ..., address: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the action shortcut function.
const dataConnect = getDataConnect(connectorConfig);
const { data } = await syncSupplier(dataConnect, syncSupplierVars);

console.log(data.supplier_upsert);

// Or, you can use the `Promise` API.
syncSupplier(syncSupplierVars).then((response) => {
  const data = response.data;
  console.log(data.supplier_upsert);
});
```

### Using `SyncSupplier`'s `MutationRef` function

```javascript
import { getDataConnect, DataConnect, executeMutation } from 'firebase/data-connect';
import { connectorConfig, syncSupplierRef, SyncSupplierVariables } from '@storebook/dataconnect';

// The `SyncSupplier` mutation requires an argument of type `SyncSupplierVariables`:
const syncSupplierVars: SyncSupplierVariables = {
  id: ..., 
  storeId: ..., 
  name: ..., 
  phone: ..., // optional
  gstin: ..., // optional
  address: ..., // optional
  isDeleted: ..., 
  updatedAt: ..., 
};

// Call the `syncSupplierRef()` function to get a reference to the mutation.
const ref = syncSupplierRef(syncSupplierVars);
// Variables can be defined inline as well.
const ref = syncSupplierRef({ id: ..., storeId: ..., name: ..., phone: ..., gstin: ..., address: ..., isDeleted: ..., updatedAt: ..., });

// You can also pass in a `DataConnect` instance to the `MutationRef` function.
const dataConnect = getDataConnect(connectorConfig);
const ref = syncSupplierRef(dataConnect, syncSupplierVars);

// Call `executeMutation()` on the reference to execute the mutation.
// You can use the `await` keyword to wait for the promise to resolve.
const { data } = await executeMutation(ref);

console.log(data.supplier_upsert);

// Or, you can use the `Promise` API.
executeMutation(ref).then((response) => {
  const data = response.data;
  console.log(data.supplier_upsert);
});
```

