import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import { Project, MonitoredApi, User } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { FolderGit2, ArrowLeft, Plus, Users, Activity, Trash2, UserPlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';

export const ProjectDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [project, setProject] = useState<Project | null>(null);
  const [apis, setApis] = useState<MonitoredApi[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [showMemberModal, setShowMemberModal] = useState<boolean>(false);
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([]);

  const { user } = useAuth();
  const { addToast } = useToast();
  const canManage = user?.role === 'ADMIN' || user?.role === 'DEVELOPER';

  const fetchProjectData = async () => {
    try {
      const [projRes, apisRes] = await Promise.all([
        api.get(`/projects/${id}`),
        api.get('/apis?page=0&size=100'),
      ]);
      setProject(projRes.data.data);
      // Filter APIs belonging to this project
      const filtered = apisRes.data.data.content.filter((a: MonitoredApi) => a.projectId === Number(id));
      setApis(filtered);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjectData();
    if (user?.role === 'ADMIN') {
      api.get('/users?page=0&size=100').then((res) => {
        setAvailableUsers(res.data.data.content);
      });
    }
  }, [id, user]);

  const handleAddMembers = async () => {
    try {
      await api.post(`/projects/${id}/members`, {
        userIds: selectedUserIds,
      });
      addToast({ type: 'success', title: 'Members added to project' });
      setShowMemberModal(false);
      setSelectedUserIds([]);
      fetchProjectData();
    } catch (err: any) {
      addToast({ type: 'error', title: 'Failed to add members', message: err.response?.data?.message });
    }
  };

  const handleRemoveMember = async (userId: number) => {
    if (!window.confirm('Remove this member from the project?')) return;
    try {
      await api.delete(`/projects/${id}/members/${userId}`);
      addToast({ type: 'success', title: 'Member removed' });
      fetchProjectData();
    } catch (err: any) {
      addToast({ type: 'error', title: 'Action failed', message: err.response?.data?.message });
    }
  };

  if (loading || !project) {
    return <div className="py-12 text-center text-xs text-slate-500">Loading project details...</div>;
  }

  return (
    <div className="space-y-6">
      {/* Back button & Header */}
      <div>
        <Link
          to="/projects"
          className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-slate-200 mb-3"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Projects</span>
        </Link>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <FolderGit2 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold tracking-tight text-white">{project.name}</h2>
              <p className="text-xs text-slate-400">
                Owned by <span className="text-slate-200">{project.owner?.fullName}</span> · Created on{' '}
                {project.createdAt?.split('T')[0]}
              </p>
            </div>
          </div>
          {canManage && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => setShowMemberModal(true)}
                className="px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-200 hover:bg-slate-800 transition-colors flex items-center gap-1.5"
              >
                <UserPlus className="w-3.5 h-3.5 text-slate-400" />
                <span>Add Member</span>
              </button>
              <Link
                to="/apis"
                className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5"
              >
                <Plus className="w-4 h-4" />
                <span>Register API</span>
              </Link>
            </div>
          )}
        </div>
      </div>

      {/* Description */}
      {project.description && (
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-300">
          {project.description}
        </div>
      )}

      {/* Two Column Grid: Members & APIs */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Monitored APIs in this Project */}
        <div className="lg:col-span-2 p-5 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                <Activity className="w-4 h-4 text-emerald-400" />
                <span>Monitored APIs ({apis.length})</span>
              </h3>
              <p className="text-xs text-slate-400">Services attached to this project workspace</p>
            </div>
          </div>

          <div className="divide-y divide-slate-800/60">
            {apis.length === 0 ? (
              <div className="py-8 text-center text-xs text-slate-500">
                No APIs registered in this project yet.
              </div>
            ) : (
              apis.map((api) => (
                <div key={api.id} className="py-3 flex items-center justify-between gap-4">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-semibold">
                        {api.httpMethod}
                      </span>
                      <Link
                        to={`/apis/${api.id}`}
                        className="text-xs font-semibold text-slate-200 hover:text-emerald-400 truncate"
                      >
                        {api.name}
                      </Link>
                    </div>
                    <p className="text-[11px] text-slate-500 font-mono truncate mt-0.5">{api.endpointUrl}</p>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <span className="text-xs text-slate-400 font-mono">
                      {api.lastResponseTimeMs ? `${api.lastResponseTimeMs}ms` : '—'}
                    </span>
                    <StatusBadge status={api.currentStatus} type="api" />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Team Members List */}
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                <Users className="w-4 h-4 text-slate-400" />
                <span>Assigned Team ({project.members?.length || 1})</span>
              </h3>
              <p className="text-xs text-slate-400">Users with workspace access</p>
            </div>
          </div>

          <div className="divide-y divide-slate-800/60 flex-1">
            {project.members?.map((member) => (
              <div key={member.id} className="py-2.5 flex items-center justify-between gap-3">
                <div className="flex items-center gap-2.5 min-w-0">
                  <div className="h-7 w-7 rounded-full bg-slate-800 flex items-center justify-center text-xs font-bold text-slate-300 uppercase">
                    {member.fullName.charAt(0)}
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-medium text-slate-200 truncate">{member.fullName}</p>
                    <p className="text-[10px] text-slate-500 truncate">{member.email}</p>
                  </div>
                </div>
                {canManage && member.id !== project.owner?.id && (
                  <button
                    onClick={() => handleRemoveMember(member.id)}
                    className="p-1 rounded text-slate-500 hover:text-rose-400 transition-colors"
                    title="Remove member"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Add Member Modal */}
      {showMemberModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
          <div className="w-full max-w-sm rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-2xl">
            <h3 className="text-base font-bold text-white mb-1">Add Members</h3>
            <p className="text-xs text-slate-400 mb-4">Select team users to add to this project.</p>

            <div className="max-h-48 overflow-y-auto divide-y divide-slate-800 bg-slate-950 rounded-lg border border-slate-800 p-2 mb-4">
              {availableUsers.map((u) => (
                <label key={u.id} className="flex items-center gap-2 p-1.5 text-xs text-slate-300 cursor-pointer hover:bg-slate-900 rounded">
                  <input
                    type="checkbox"
                    checked={selectedUserIds.includes(u.id)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedUserIds([...selectedUserIds, u.id]);
                      } else {
                        setSelectedUserIds(selectedUserIds.filter((id) => id !== u.id));
                      }
                    }}
                    className="rounded border-slate-700 bg-slate-800 text-emerald-500 focus:ring-0"
                  />
                  <span>{u.fullName} ({u.role})</span>
                </label>
              ))}
            </div>

            <div className="flex items-center justify-end gap-2">
              <button
                onClick={() => setShowMemberModal(false)}
                className="px-3 py-1.5 rounded-lg bg-slate-800 text-xs text-slate-300 hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                onClick={handleAddMembers}
                className="px-3 py-1.5 rounded-lg bg-emerald-500 text-slate-950 text-xs font-semibold hover:bg-emerald-600"
              >
                Add Selected
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
