'use client';

import { useState } from 'react';
import { Settings, Megaphone, Tag, Power, AlertTriangle, Shield } from 'lucide-react';

export default function SettingsClient({ 
  initialSettings, 
  initialPromoCodes, 
  initialAnnouncements 
}: { 
  initialSettings: Record<string, unknown>[], 
  initialPromoCodes: Record<string, unknown>[], 
  initialAnnouncements: Record<string, unknown>[] 
}) {
  const [activeTab, setActiveTab] = useState<'config' | 'promo' | 'announcements'>('config');

  // Mapped configuration state
  const isMaintenance = initialSettings.find(s => s.key === 'maintenance_mode')?.value === 'true';

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Settings className="text-teal-500" />
            Platform Settings
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Configure global platform constants, promos, and broadcast messages.</p>
        </div>
      </div>

      <div className="flex space-x-2 border-b border-gray-200 dark:border-gray-800 mb-6">
        <button 
          onClick={() => setActiveTab('config')}
          className={`pb-3 px-4 text-sm font-medium transition-colors ${activeTab === 'config' ? 'border-b-2 border-teal-500 text-teal-600 dark:text-teal-400' : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'}`}
        >
          <div className="flex items-center gap-2"><Shield size={16}/> Global Config & Flags</div>
        </button>
        <button 
          onClick={() => setActiveTab('promo')}
          className={`pb-3 px-4 text-sm font-medium transition-colors ${activeTab === 'promo' ? 'border-b-2 border-teal-500 text-teal-600 dark:text-teal-400' : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'}`}
        >
          <div className="flex items-center gap-2"><Tag size={16}/> Promo Codes</div>
        </button>
        <button 
          onClick={() => setActiveTab('announcements')}
          className={`pb-3 px-4 text-sm font-medium transition-colors ${activeTab === 'announcements' ? 'border-b-2 border-teal-500 text-teal-600 dark:text-teal-400' : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'}`}
        >
          <div className="flex items-center gap-2"><Megaphone size={16}/> Announcements</div>
        </button>
      </div>

      {activeTab === 'config' && (
        <div className="space-y-6">
          <div className="glass-card p-6 border border-red-200 dark:border-red-900/30 rounded-xl bg-red-50/50 dark:bg-red-900/10">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-bold text-red-700 dark:text-red-400 flex items-center gap-2">
                  <AlertTriangle size={20} />
                  Maintenance Mode
                </h3>
                <p className="text-sm text-red-600/80 dark:text-red-300/80 mt-1">
                  Enabling this will immediately lock out ALL users (except super admins) and display a maintenance screen.
                </p>
              </div>
              <button 
                onClick={() => alert("Maintenance Mode toggled!")}
                className={`px-6 py-2.5 rounded-xl font-bold transition-colors flex items-center gap-2 ${isMaintenance ? 'bg-red-600 hover:bg-red-700 text-white' : 'bg-gray-200 hover:bg-gray-300 dark:bg-gray-800 dark:hover:bg-gray-700 text-gray-800 dark:text-gray-200'}`}
              >
                <Power size={18} />
                {isMaintenance ? 'DISABLE Maintenance' : 'ENABLE Maintenance'}
              </button>
            </div>
          </div>

          <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">Feature Flags</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg">
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white">Enable AI Analytics Engine</h4>
                  <p className="text-xs text-gray-500">Global flag to expose the &apos;StoreBook AI&apos; sidebar to Premium users.</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input aria-label="checkbox" type="checkbox" className="sr-only peer" defaultChecked />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-teal-500"></div>
                </label>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'promo' && (
        <div className="glass-card border border-gray-200 dark:border-gray-800 rounded-xl">
          <div className="p-6 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">Active Promo Codes</h3>
            <button className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white text-sm font-medium rounded-lg transition-colors">
              + New Promo
            </button>
          </div>
          <div className="p-6 text-center text-gray-500 text-sm">
            {initialPromoCodes.length > 0 ? (
              <pre className="text-left bg-gray-100 dark:bg-gray-800 p-4 rounded-lg overflow-x-auto text-xs">{JSON.stringify(initialPromoCodes, null, 2)}</pre>
            ) : (
              <p>No active promo codes found. Create one to drive subscription sales.</p>
            )}
          </div>
        </div>
      )}

      {activeTab === 'announcements' && (
        <div className="glass-card border border-gray-200 dark:border-gray-800 rounded-xl">
          <div className="p-6 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">Global Broadcasts</h3>
            <button className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white text-sm font-medium rounded-lg transition-colors">
              + New Broadcast
            </button>
          </div>
          <div className="p-6 text-center text-gray-500 text-sm">
            {initialAnnouncements.length > 0 ? (
              <pre className="text-left bg-gray-100 dark:bg-gray-800 p-4 rounded-lg overflow-x-auto text-xs">{JSON.stringify(initialAnnouncements, null, 2)}</pre>
            ) : (
              <p>No active announcements. Broadcast messages will appear globally at the top of every store owner&apos;s dashboard.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
