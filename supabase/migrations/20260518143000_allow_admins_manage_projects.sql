-- Allow admin users to manage projects through the app.

drop policy if exists "Admins can read projects" on public.projetos;
drop policy if exists "Admins can create projects" on public.projetos;
drop policy if exists "Admins can update projects" on public.projetos;
drop policy if exists "Admins can delete projects" on public.projetos;

create policy "Admins can read projects"
on public.projetos
for select
to authenticated
using (public.current_user_is_admin());

create policy "Admins can create projects"
on public.projetos
for insert
to authenticated
with check (public.current_user_is_admin());

create policy "Admins can update projects"
on public.projetos
for update
to authenticated
using (public.current_user_is_admin())
with check (public.current_user_is_admin());

create policy "Admins can delete projects"
on public.projetos
for delete
to authenticated
using (public.current_user_is_admin());
