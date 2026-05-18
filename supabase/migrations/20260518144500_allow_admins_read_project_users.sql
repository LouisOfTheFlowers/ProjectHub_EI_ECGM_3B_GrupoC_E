-- Allow admin users to read project memberships for dashboard counts.

drop policy if exists "Admins can read project users" on public.projeto_users;
drop policy if exists "Admins can delete project users" on public.projeto_users;

create policy "Admins can read project users"
on public.projeto_users
for select
to authenticated
using (public.current_user_is_admin());

create policy "Admins can delete project users"
on public.projeto_users
for delete
to authenticated
using (public.current_user_is_admin());
