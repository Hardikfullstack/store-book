import './globals.css';
import Sidebar from '@/components/Sidebar';
import { ThemeProvider } from '@/components/ThemeProvider';
import { Inter } from 'next/font/google';
import { getSession } from '@/lib/session';

const inter = Inter({ subsets: ['latin'], display: 'swap', preload: false });

export const metadata = {
  title: 'StoreBook Pro Dashboard',
  description: 'Manage your store items, sales, udhaar, and expenses',
};

export default async function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const session = await getSession();

  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <link rel="manifest" href="/manifest.json" />
        <meta name="theme-color" content="#0d9488" />
      </head>
      <body suppressHydrationWarning className={`${inter.className} bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100 antialiased`}>
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
          <div className="flex min-h-screen">
            <Sidebar session={session} />
            <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50/30 dark:bg-gray-950/30">
              <div className="max-w-7xl mx-auto px-8 py-8">
                {children}
              </div>
            </main>
          </div>
        </ThemeProvider>
      </body>
    </html>
  );
}
