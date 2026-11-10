import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { User, Project } from '../types';
import { Users, FolderGit2, Shield, Mail, Phone, Calendar } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Team: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const { user } = useAuth();

  useEffect(() => {
    const fetchTeam = async () => {
      try {
        const [usersRes, projRes] = await Promise.all([
          api.get('/users?page=0&size=100').catch(() => ({ data: { data: { content: [] } } })),
          api.get('/projects?page=0&size=50'),
        ]);
        setUsers(usersRes.data.data.content);
        setProjects(projRes.data.data.content);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchTeam();
  }, []);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
          Engineering Team & Project Ownership
        </h2>
        <p className="text-xs text-slate-400 mt-1">
          Role-based operational access across platform workspaces and incident response teams
        </p>
      </div>

      {/* Team Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">Total Team Members</span>
          <p className="text-2xl font-bold text-white mt-1.5">{users.length || 1}</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">Active collaborators</span>
        </div>
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">Total Workspaces</span>
          <p className="text-2xl font-bold text-emerald-400 mt-1.5">{projects.length}</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">Shared project teams</span>
        </div>
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">Your Role</span>
          <p className="text-2xl font-bold text-sky-400 mt-1.5">{user?.role}</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">{user?.email}</span>
        </div>
      </div>

      {/* Users Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {users.map((member) => (
          <div
            key={member.id}
            className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-slate-800 flex items-center justify-center text-sm font-bold text-emerald-400 uppercase border border-slate-700">
                    {member.fullName.charAt(0)}
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-slate-100">{member.fullName}</h3>
                    <span
                      className={`text-[10px] font-mono px-2 py-0.5 rounded font-bold uppercase inline-block mt-0.5 ${
                        member.role === 'ADMIN'
                          ? 'bg-purple-950/80 text-purple-400 border border-purple-800'
                          : member.role === 'DEVELOPER'
                          ? 'bg-emerald-950/80 text-emerald-400 border border-emerald-800'
                          : 'bg-slate-800 text-slate-400 border border-slate-700'
                      }`}
                    >
                      {member.role}
                    </span>
                  </div>
                </div>
              </div>

              <div className="space-y-1.5 text-xs text-slate-400 mt-3 pt-3 border-t border-slate-800/80">
                <div className="flex items-center gap-2 truncate">
                  <Mail className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                  <span className="truncate">{member.email}</span>
                </div>
                {member.phone && (
                  <div className="flex items-center gap-2">
                    <Phone className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                    <span>{member.phone}</span>
                  </div>
                )}
                <div className="flex items-center gap-2 text-slate-500 text-[11px]">
                  <Calendar className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                  <span>Joined {member.createdAt?.split('T')[0]}</span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

