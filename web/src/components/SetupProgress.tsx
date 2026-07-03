'use client';

import { useEffect, useState } from 'react';
import { CheckCircle2, Loader2 } from 'lucide-react';

type SetupProgressProps = {
  open: boolean;
  title?: string;
  message?: string;
  progress?: number;
  completed?: boolean;
};

export default function SetupProgress({
  open,
  title = 'Setting up your store',
  message = 'We are preparing your dashboard and syncing your data.',
  progress,
  completed = false,
}: SetupProgressProps) {
  const [displayProgress, setDisplayProgress] = useState(progress ?? 0);

  useEffect(() => {
    if (!open) {
      setDisplayProgress(0);
      return;
    }

    if (typeof progress === 'number') {
      setDisplayProgress(progress);
      return;
    }

    const interval = window.setInterval(() => {
      setDisplayProgress((value) => (value < 90 ? value + 8 : value));
    }, 220);

    return () => window.clearInterval(interval);
  }, [open, progress]);

  if (!open) return null;

  const safeProgress = Math.min(Math.max(displayProgress, 0), 100);

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-white/90 px-6 backdrop-blur-sm dark:bg-gray-950/90">
      <div className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-8 text-center shadow-2xl dark:border-gray-800 dark:bg-gray-900">
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-teal-100 text-teal-600 dark:bg-teal-900/30 dark:text-teal-400">
          {completed ? <CheckCircle2 size={28} /> : <Loader2 size={28} className="animate-spin" />}
        </div>

        <h2 className="text-xl font-semibold text-gray-900 dark:text-white">{title}</h2>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">{message}</p>

        <div className="mt-6 h-2.5 overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
          <div
            className="h-full rounded-full bg-gradient-to-r from-teal-500 to-emerald-500 transition-all duration-300"
            style={{ width: `${safeProgress}%` }}
          />
        </div>

        <p className="mt-3 text-xs font-medium uppercase tracking-[0.24em] text-gray-500 dark:text-gray-400">
          {completed ? 'Ready' : `${Math.round(safeProgress)}% complete`}
        </p>
      </div>
    </div>
  );
}
