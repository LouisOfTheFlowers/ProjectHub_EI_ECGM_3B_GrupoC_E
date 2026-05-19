-- Allow admin users to manage tasks through the app.

drop policy if exists "Admins can read tasks" on public.tarefas;
drop policy if exists "Admins can create tasks" on public.tarefas;
drop policy if exists "Admins can update tasks" on public.tarefas;
drop policy if exists "Admins can delete tasks" on public.tarefas;

create policy "Admins can read tasks"
on public.tarefas
for select
to authenticated
using (public.current_user_is_admin());

create policy "Admins can create tasks"
on public.tarefas
for insert
to authenticated
with check (public.current_user_is_admin());

create policy "Admins can update tasks"
on public.tarefas
for update
to authenticated
using (public.current_user_is_admin())
with check (public.current_user_is_admin());

create policy "Admins can delete tasks"
on public.tarefas
for delete
to authenticated
using (public.current_user_is_admin());

drop policy if exists "Admins can read task users" on public.tarefa_users;
drop policy if exists "Admins can delete task users" on public.tarefa_users;

create policy "Admins can read task users"
on public.tarefa_users
for select
to authenticated
using (public.current_user_is_admin());

create policy "Admins can delete task users"
on public.tarefa_users
for delete
to authenticated
using (public.current_user_is_admin());
