'use client';

import { useState } from 'react';
import { Phone, Lock, ChevronRight } from 'lucide-react';
import { auth } from '@/lib/firebase';
import { RecaptchaVerifier, signInWithPhoneNumber, ConfirmationResult } from 'firebase/auth';
import { login } from '@/app/actions';
import { ThemeToggle } from '@/components/ThemeToggle';
import { countryCodes } from '@/lib/constants';

export default function LoginPage() {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [countryCode, setCountryCode] = useState('+91');
  const [otp, setOtp] = useState('');
  const [step, setStep] = useState<'PHONE' | 'OTP'>('PHONE');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [confirmationResult, setConfirmationResult] = useState<ConfirmationResult | null>(null);

  // Staff login state
  const [isStaff, setIsStaff] = useState(false);
  const [staffUsername, setStaffUsername] = useState('');
  const [staffPassword, setStaffPassword] = useState('');

  const formatPhoneNumber = (number: string) => {
    // Prefix with selected country code if it doesn't already start with '+'
    if (!number.startsWith('+')) {
      return `${countryCode}${number}`;
    }
    return number;
  };

  const setupRecaptcha = () => {
    if (!window.recaptchaVerifier) {
      window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', {
        size: 'invisible'
      });
    }
  };

  const handleSendOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      setupRecaptcha();
      const formattedPhone = formatPhoneNumber(phoneNumber);
      const appVerifier = window.recaptchaVerifier;
      const confirmation = await signInWithPhoneNumber(auth, formattedPhone, appVerifier);
      setConfirmationResult(confirmation);
      setStep('OTP');
    } catch (err: any) {
      console.error(err);
      setError(err.message || 'Failed to send OTP. Please try again.');
      // Reset recaptcha on error
      if (window.recaptchaVerifier) {
        window.recaptchaVerifier.clear();
        window.recaptchaVerifier = undefined;
      }
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!confirmationResult) return;
    
    setLoading(true);
    setError('');

    try {
      const result = await confirmationResult.confirm(otp);
      const user = result.user;
      
      const idToken = await user.getIdToken();
      const res = await login(idToken);
      
      if (res.success) {
        window.location.href = '/'; // redirect to dashboard
      } else {
        setError(res.error || 'Failed to create session');
      }

    } catch (err: any) {
      console.error(err);
      setError(err.message || 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleStaffLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const dummyEmail = `${staffUsername.toLowerCase().replace(/[^a-z0-9]/g, '')}@storebook.internal`;
      const { signInWithEmailAndPassword } = await import('firebase/auth');
      const userCredential = await signInWithEmailAndPassword(auth, dummyEmail, staffPassword);
      const idToken = await userCredential.user.getIdToken();
      const res = await login(idToken);
      
      if (res.success) {
        window.location.href = '/';
      } else {
        setError(res.error || 'Failed to create session');
      }
    } catch (err: any) {
      console.error(err);
      setError('Invalid username or password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
      <div className="absolute top-0 right-0 p-4">
        <ThemeToggle />
      </div>
      <div className="w-full max-w-md">
        <div className="glass-card p-8 rounded-2xl shadow-xl border border-gray-100 dark:border-gray-800">
          <div className="text-center mb-8">
            <div className="bg-teal-500 w-16 h-16 rounded-2xl mx-auto flex items-center justify-center mb-4 shadow-lg shadow-teal-500/30">
              <Lock className="text-white" size={32} />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">StoreBook Portal</h1>
            <p className="text-gray-500 dark:text-gray-400 mt-2">Login or create an account</p>
          </div>

          {error && (
            <div className="mb-6 p-3 rounded-lg bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 text-sm border border-red-100 dark:border-red-800">
              {error}
            </div>
          )}

          <div className="flex rounded-lg bg-gray-100 dark:bg-gray-800 p-1 mb-8">
            <button
              className={`flex-1 py-2 text-sm font-medium rounded-md transition-all ${!isStaff ? 'bg-white dark:bg-gray-700 shadow text-teal-600' : 'text-gray-500'}`}
              onClick={() => { setIsStaff(false); setError(''); }}
            >
              Owner
            </button>
            <button
              className={`flex-1 py-2 text-sm font-medium rounded-md transition-all ${isStaff ? 'bg-white dark:bg-gray-700 shadow text-teal-600' : 'text-gray-500'}`}
              onClick={() => { setIsStaff(true); setError(''); }}
            >
              Staff
            </button>
          </div>

          {!isStaff ? (
            step === 'PHONE' ? (
            <form onSubmit={handleSendOtp} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Mobile Number</label>
                <div className="relative flex rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 focus-within:ring-2 focus-within:ring-teal-500 transition-all overflow-hidden">
                  <div className="flex items-center pl-3 bg-gray-100 dark:bg-gray-900 border-r border-gray-200 dark:border-gray-700">
                    <Phone className="h-4 w-4 text-gray-500 mr-1" />
                    <select
                      value={countryCode}
                      onChange={(e) => setCountryCode(e.target.value)}
                      className="bg-transparent text-sm font-medium text-gray-700 dark:text-gray-300 border-none focus:ring-0 py-3 pr-2 pl-1 cursor-pointer outline-none appearance-none"
                    >
                      {countryCodes.map((c) => (
                        <option key={c.country + c.code} value={c.code} className="text-gray-900 dark:text-gray-900">
                          {c.country} ({c.code})
                        </option>
                      ))}
                    </select>
                  </div>
                  <input
                    type="tel"
                    required
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value.replace(/[^0-9]/g, ''))}
                    className="block w-full px-3 py-3 bg-transparent text-gray-900 dark:text-white border-none focus:ring-0 outline-none"
                    placeholder="Enter mobile number"
                  />
                </div>
              </div>

              <div id="recaptcha-container"></div>

              <button
                type="submit"
                disabled={loading}
                className="w-full btn-primary flex justify-center items-center py-3 rounded-xl disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                ) : (
                  <>
                    <span>Send OTP</span>
                    <ChevronRight size={18} className="ml-2" />
                  </>
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Enter OTP</label>
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">
                  Sent to {formatPhoneNumber(phoneNumber)}{' '}
                  <button type="button" onClick={() => setStep('PHONE')} className="text-teal-600 hover:underline">Change</button>
                </p>
                <div className="flex gap-2">
                  <input
                    type="text"
                    required
                    maxLength={6}
                    value={otp}
                    onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))}
                    className="block w-full text-center tracking-[0.5em] font-mono text-2xl py-3 border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all"
                    placeholder="------"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading || otp.length !== 6}
                className="w-full btn-primary flex justify-center items-center py-3 rounded-xl disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                ) : (
                  <span>Verify & Login</span>
                )}
              </button>
            </form>
          )) : (
            <form onSubmit={handleStaffLogin} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Username</label>
                <input
                  type="text"
                  required
                  value={staffUsername}
                  onChange={(e) => setStaffUsername(e.target.value)}
                  className="block w-full px-3 py-3 border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all"
                  placeholder="Enter username"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Password / PIN</label>
                <input
                  type="password"
                  required
                  value={staffPassword}
                  onChange={(e) => setStaffPassword(e.target.value)}
                  className="block w-full px-3 py-3 border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all"
                  placeholder="Enter password"
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="w-full btn-primary flex justify-center items-center py-3 rounded-xl disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                ) : (
                  <span>Login</span>
                )}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

// Add type for window.recaptchaVerifier
declare global {
  interface Window {
    recaptchaVerifier: any;
  }
}
