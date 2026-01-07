import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthContext } from '../../context/AuthContext';
import ThemeToggle from '../common/ThemeToggle';
import NotificationBell from '../common/NotificationBell';
import CommandPalette from '../common/CommandPalette';
import { Search, ChevronDown, User, Settings, LogOut, Menu, X, Command } from 'lucide-react';

interface NavbarProps {
    onMenuClick: () => void;
    sidebarOpen: boolean;
}

const Navbar: React.FC<NavbarProps> = ({ onMenuClick, sidebarOpen }) => {
    const { user, logout } = useAuthContext();
    const [showProfileMenu, setShowProfileMenu] = useState(false);
    const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);
    const navigate = useNavigate();

    // Safe user accessors
    const getUserInitial = () => {
        const email = user?.email;
        if (!email) return 'U';
        return email.charAt(0).toUpperCase();
    };
    const getUserDisplay = () => {
        const email = user?.email;
        if (!email) return 'CORE_PERSON';
        return email.split('@')[0]?.toUpperCase() || 'CORE_PERSON';
    };
    const getUserEmail = () => user?.email || 'N/A';
    const getUserRole = () => {
        const roles = user?.roles;
        if (!roles || roles.length === 0) return 'Personnel';
        return roles[0]?.name || 'Personnel';
    };

    return (
        <>
            <nav className="fixed top-0 left-0 right-0 h-16 glass-effect z-50 border-b border-white/10 dark:border-white/5 transition-all duration-300">
                <div className="h-full px-4 lg:px-8 flex items-center justify-between">
                    {/* Left Section */}
                    <div className="flex items-center gap-4 lg:gap-8">
                        {/* Hamburger Menu - Tactical Style */}
                        <button
                            onClick={onMenuClick}
                            className="w-10 h-10 rounded-2xl bg-secondary-100/50 dark:bg-secondary-800/50 border border-secondary-200 dark:border-secondary-700 center hover:bg-primary-500 hover:text-white transition-all duration-500 group focus-ring overflow-hidden"
                            aria-label="Toggle sidebar"
                        >
                            {sidebarOpen ? (
                                <X className="w-5 h-5 transition-transform duration-500 group-hover:rotate-90" />
                            ) : (
                                <Menu className="w-5 h-5 transition-transform duration-500 group-hover:scale-110" />
                            )}
                        </button>

                        {/* Logo - Premium Typography */}
                        <Link to="/dashboard" className="flex items-center gap-3 group px-2 py-1.5 rounded-2xl hover:bg-white/5 transition-all">
                            <div className="w-9 h-9 bg-gradient-to-br from-primary-600 to-primary-800 rounded-xl center text-white font-black text-sm shadow-glow relative overflow-hidden group-hover:scale-105 transition-transform duration-500">
                                <div className="absolute inset-0 bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity" />
                                TX
                            </div>
                            <div className="flex flex-col leading-none">
                                <span className="font-black text-xl tracking-tighter text-secondary-900 dark:text-white group-hover:text-primary-500 transition-colors uppercase italic font-display">
                                    Talent<span className="text-primary-500">X</span>
                                </span>
                                <span className="text-[8px] font-black uppercase tracking-[0.3em] text-secondary-400 mt-0.5">Personnel Strategy</span>
                            </div>
                        </Link>
                    </div>

                    {/* Center Section - Global Intelligence Entry */}
                    <div className="hidden md:block flex-1 max-w-xl mx-8 relative">
                        <button
                            className="w-full h-11 px-4 pl-12 bg-secondary-900/5 dark:bg-white/5 border border-secondary-200/50 dark:border-white/5 rounded-2xl flex items-center justify-between group hover:border-primary-500/30 transition-all cursor-text focus:outline-none focus:ring-4 focus:ring-primary-500/10"
                            onClick={() => setIsCommandPaletteOpen(true)}
                        >
                            <div className="flex items-center gap-3">
                                <Search className="w-4 h-4 text-secondary-400 group-hover:text-primary-500 transition-colors" />
                                <span className="text-sm font-bold text-secondary-400 group-hover:text-secondary-600 dark:group-hover:text-secondary-300 transition-colors italic">Run Strategic Command...</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <kbd className="hidden lg:inline-flex px-2 py-1 rounded-lg bg-secondary-800 text-white/50 text-[10px] font-black tracking-widest gap-1 border border-white/5 shadow-inner">
                                    <Command className="w-2.5 h-2.5" /> K
                                </kbd>
                            </div>
                        </button>
                        <div className="absolute -bottom-1 left-4 right-4 h-px bg-gradient-to-r from-transparent via-primary-500/0 towards-primary-500/30 to-transparent group-hover:via-primary-500/50 transition-all duration-700" />
                    </div>

                    {/* Right Section - Interaction Hub */}
                    <div className="flex items-center gap-3 lg:gap-5">
                        {/* Theme Engine */}
                        <div className="p-1 rounded-2xl bg-secondary-100/50 dark:bg-white/5 border border-secondary-200/50 dark:border-white/5">
                            <ThemeToggle />
                        </div>

                        <div className="w-px h-8 bg-secondary-200 dark:bg-white/5 mx-1" />

                        {/* System Notifications - Unified Bell Component */}
                        <div className="relative group/bell">
                            <NotificationBell userId={user ? user.id : undefined} />
                        </div>

                        {/* Personnel Identity Control */}
                        <div className="relative" onMouseLeave={() => setShowProfileMenu(false)}>
                            <button
                                onMouseEnter={() => setShowProfileMenu(true)}
                                className={`flex items-center gap-3 pl-1 pr-3 py-1 rounded-2xl border transition-all duration-500 group/prof ${showProfileMenu ? 'bg-secondary-900 border-primary-500 shadow-glow' : 'bg-transparent border-secondary-200 dark:border-white/5 hover:border-primary-500/50'}`}
                            >
                                <div className="w-9 h-9 bg-gradient-to-tr from-secondary-100 to-secondary-200 dark:from-secondary-800 dark:to-secondary-700 rounded-xl center text-secondary-900 dark:text-white font-black text-xs shadow-inner relative overflow-hidden group-hover/prof:scale-105 transition-transform border border-secondary-200 dark:border-white/10">
                                    {getUserInitial()}
                                    <div className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-success-500 rounded-full border-2 border-white dark:border-secondary-900 shadow-sm" />
                                </div>
                                <div className="hidden lg:flex flex-col text-left">
                                    <p className={`text-xs font-black tracking-tight transition-colors ${showProfileMenu ? 'text-white' : 'text-secondary-900 dark:text-white'}`}>
                                        {getUserDisplay()}
                                    </p>
                                    <p className={`text-[9px] font-black uppercase tracking-widest transition-colors ${showProfileMenu ? 'text-primary-400' : 'text-secondary-400'}`}>
                                        {getUserRole()}
                                    </p>
                                </div>
                                <ChevronDown className={`w-3.5 h-3.5 text-secondary-400 transition-all duration-500 ${showProfileMenu ? 'rotate-180 text-primary-500' : 'group-hover/prof:text-secondary-600'}`} />
                            </button>

                            {/* Profile Matrix Dropdown */}
                            {showProfileMenu && (
                                <div className="absolute right-0 top-[calc(100%+8px)] w-64 premium-card p-2 animate-slide-up z-[100] bg-secondary-900 border-primary-500/50 shadow-2xl">
                                    <div className="px-4 py-4 border-b border-white/5 mb-2 relative overflow-hidden group/header">
                                        <div className="absolute top-0 right-0 w-24 h-24 bg-primary-600/10 blur-[40px] rounded-full group-hover/header:bg-primary-600/20 transition-all" />
                                        <p className="text-[10px] font-black uppercase tracking-[0.2em] text-primary-500 mb-1">Authenticated Asset</p>
                                        <p className="text-sm font-black text-white truncate italic tracking-tighter">{getUserEmail()}</p>
                                        <div className="flex gap-2 mt-2">
                                            <span className="px-2 py-0.5 rounded-md bg-white/5 text-[9px] font-black text-secondary-400 uppercase tracking-widest border border-white/5">TX-10042</span>
                                            <span className="px-2 py-0.5 rounded-md bg-success-500/10 text-[9px] font-black text-success-500 uppercase tracking-widest border border-success-500/20 flex items-center gap-1">
                                                <div className="w-1 h-1 rounded-full bg-success-500 animate-pulse" /> SYNCED
                                            </span>
                                        </div>
                                    </div>
                                    <div className="space-y-1">
                                        <Link
                                            to="/profile"
                                            className="px-3 py-2.5 rounded-xl hover:bg-white/5 flex items-center gap-3 transition-colors group/item"
                                            onClick={() => setShowProfileMenu(false)}
                                        >
                                            <div className="w-8 h-8 rounded-lg bg-white/5 center text-secondary-400 group-hover/item:text-primary-500 group-hover/item:bg-primary-500/10 transition-all">
                                                <User className="w-4 h-4" />
                                            </div>
                                            <span className="text-xs font-black uppercase tracking-widest text-secondary-300 group-hover/item:text-white transition-colors">Strategic Profile</span>
                                        </Link>
                                        <Link
                                            to="/settings"
                                            className="px-3 py-2.5 rounded-xl hover:bg-white/5 flex items-center gap-3 transition-colors group/item"
                                            onClick={() => setShowProfileMenu(false)}
                                        >
                                            <div className="w-8 h-8 rounded-lg bg-white/5 center text-secondary-400 group-hover/item:text-primary-500 group-hover/item:bg-primary-500/10 transition-all">
                                                <Settings className="w-4 h-4" />
                                            </div>
                                            <span className="text-xs font-black uppercase tracking-widest text-secondary-300 group-hover/item:text-white transition-colors">Control Center</span>
                                        </Link>
                                    </div>
                                    <div className="pt-2 mt-2 border-t border-white/5">
                                        <button
                                            onClick={() => {
                                                setShowProfileMenu(false);
                                                logout();
                                                navigate('/login');
                                            }}
                                            className="w-full px-3 py-2.5 rounded-xl hover:bg-danger-500/10 flex items-center gap-3 transition-all group/logout"
                                        >
                                            <div className="w-8 h-8 rounded-lg bg-danger-500/10 center text-danger-500 group-hover/logout:bg-danger-500 group-hover/logout:text-white transition-all">
                                                <LogOut className="w-4 h-4" />
                                            </div>
                                            <span className="text-xs font-black uppercase tracking-widest text-danger-500 group-hover/logout:tracking-[0.15em] transition-all">Deactivate Session</span>
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </nav>
            <CommandPalette isOpen={isCommandPaletteOpen} onClose={() => setIsCommandPaletteOpen(false)} />
        </>
    );
};

export default Navbar;

