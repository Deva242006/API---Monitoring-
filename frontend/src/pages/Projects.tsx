import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { Project, User } from '../types';
import { FolderGit2, Plus, Users, Calendar, ArrowRight, Trash2, Search } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';

export const Projects: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>('');
  const [showModal, setShowModal] = useState<boolean>(false);
  const [name, setName] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [selectedMembers, setSelectedMembers] = useState<number[]>([]);

  const { user } = useAuth();
  const { addToast } = useToast();
  const canManage = user?.role === 'ADMIN' || user?.role === 'DEVELOPER';

  const fetchProjects = async () => {
    try {
      const res = await api.get('/projects?page=0&size=50');
      setProjects(res.data.data.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const res = await api.get('/users?page=0&size=100');
      setAvailableUsers(res.data.data.content);
    } catch {
      // Non-admins might not be able to list all users, which is fine
    }
  };

  useEffect(() => {
    fetchProjects();
    if (user?.role === 'ADMIN') {
      fetchUsers();
    }
  }, [user]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/projects', {
        name,
        description,
        memberIds: selectedMembers,
      });
      addToast({ type: 'success', title: 'Project created successfully' });
      setShowModal(false);
      setName('');
      setDescription('');
      setSelectedMembers([]);
      fetchProjects();
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Failed to create project',
        message: err.response?.data?.message || 'Server error',
      });
    }
  };

  const handleDelete = async (id: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this project?')) return;

    try {
      await api.delete(`/projects/${id}`);
      addToast({ type: 'success', title: 'Project deleted' });
      fetchProjects();
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Delete failed',
        message: err.response?.data?.message || 'Server error',
      });
    }
  };

  const filteredProjects = projects.filter(
    (p) =>
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.description?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            Projects Management
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Organize APIs, services, and microservices under collaborative team projects
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowModal(true)}
            className="px-3.5 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5 self-start shadow-sm"
          >
            <Plus className="w-4 h-4" />
            <span>New Project</span>
          </button>
        )}
      </div>

      {/* Search Input */}
      <div className="relative max-w-md">
        <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search projects..."
          className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-10 pr-4 py-2 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-emerald-500/60"
        />
      </div>

      {/* Projects Grid */}
      {loading ? (
        <div className="py-12 text-center text-xs text-slate-500">Loading projects...</div>
      ) : filteredProjects.length === 0 ? (
        <div className="p-12 text-center rounded-xl bg-slate-900 border border-slate-800 flex flex-col items-center gap-3">
          <FolderGit2 className="w-10 h-10 text-slate-600" />
          <h3 className="text-sm font-semibold text-slate-300">No projects found</h3>
          <p className="text-xs text-slate-500 max-w-sm">
            Projects allow you to group monitored microservices and grant specific team permissions.
          </p>
          {canManage && (
            <button
              onClick={() => setShowModal(true)}
              className="mt-2 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-200"
            >
              Create your first project
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredProjects.map((project) => (
            <Link
              key={project.id}
              to={`/projects/${project.id}`}
              className="p-5 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 hover:bg-slate-800/40 transition-all flex flex-col group relative"
            >
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="flex items-center gap-2.5">
                  <div className="h-9 w-9 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 group-hover:bg-emerald-500/20 transition-colors">
                    <FolderGit2 className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-slate-100 group-hover:text-emerald-400 transition-colors">
                      {project.name}
                    </h3>
                    <p className="text-[11px] text-slate-500">Owner: {project.owner?.fullName}</p>
                  </div>
                </div>
                {canManage && (
                  <button
                    onClick={(e) => handleDelete(project.id, e)}
                    className="p-1.5 rounded text-slate-500 hover:text-rose-400 hover:bg-rose-950/20 transition-colors opacity-0 group-hover:opacity-100"
                    title="Delete project"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>

              <p className="text-xs text-slate-400 line-clamp-2 mb-4 flex-1">
                {project.description || 'No description provided.'}
              </p>

              <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-500">
                <div className="flex items-center gap-3">
                  <span className="flex items-center gap-1 font-mono">
                    <Users className="w-3.5 h-3.5 text-slate-400" />
                    {project.members?.length || 1}
                  </span>
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    {project.createdAt?.split('T')[0]}
                  </span>
                </div>
                <div className="flex items-center gap-1 text-emerald-400 font-medium group-hover:translate-x-0.5 transition-transform">
                  <span>View</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Create Project Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-2xl">
            <h3 className="text-base font-bold text-white mb-1">Create New Project</h3>
            <p className="text-xs text-slate-400 mb-5">
              Enter a project name to group and monitor your microservices.
            </p>

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Project Name *</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Payment Gateway Services"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-emerald-500/60"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Description</label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Briefly describe what services belong in this project..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-emerald-500/60 resize-none"
                />
              </div>

              {availableUsers.length > 0 && (
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">Add Team Members</label>
                  <div className="max-h-32 overflow-y-auto divide-y divide-slate-800 bg-slate-950 rounded-lg border border-slate-800 p-2">
                    {availableUsers.map((u) => (
                      <label key={u.id} className="flex items-center gap-2 p-1.5 text-xs text-slate-300 cursor-pointer hover:bg-slate-900 rounded">
                        <input
                          type="checkbox"
                          checked={selectedMembers.includes(u.id)}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setSelectedMembers([...selectedMembers, u.id]);
                            } else {
                              setSelectedMembers(selectedMembers.filter((id) => id !== u.id));
                            }
                          }}
                          className="rounded border-slate-700 bg-slate-800 text-emerald-500 focus:ring-0"
                        />
                        <span>{u.fullName} ({u.email})</span>
                      </label>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex items-center justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-300 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors"
                >
                  Create Project
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

